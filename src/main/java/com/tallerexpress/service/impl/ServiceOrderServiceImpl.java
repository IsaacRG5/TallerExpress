package com.tallerexpress.service.impl;

import com.tallerexpress.repository.ClientDao;
import com.tallerexpress.repository.ServiceOrderDao;
import com.tallerexpress.repository.SparePartDao;
import com.tallerexpress.repository.VehicleDao;
import com.tallerexpress.exception.BusinessException;
import com.tallerexpress.exception.StockException;
import com.tallerexpress.model.OrderPart;
import com.tallerexpress.model.OrderStatus;
import com.tallerexpress.model.ServiceOrder;
import com.tallerexpress.model.SparePart;
import com.tallerexpress.model.Vehicle;
import com.tallerexpress.config.Db;
import com.tallerexpress.config.HttpLog;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class ServiceOrderServiceImpl implements com.tallerexpress.service.ServiceOrderService {
    private final ServiceOrderDao orders;
    private final ClientDao clients;
    private final VehicleDao vehicles;
    private final SparePartDao parts;

    public ServiceOrderServiceImpl(ServiceOrderDao orders, ClientDao clients, VehicleDao vehicles, SparePartDao parts) {
        this.orders = orders;
        this.clients = clients;
        this.vehicles = vehicles;
        this.parts = parts;
    }

    @Override
    public int create(ServiceOrder order) throws BusinessException {
        validate(order);
        try (Connection c = Db.getConnection()) {
            c.setAutoCommit(false);
            try {
                var client = clients.findById(order.getClientId())
                        .orElseThrow(() -> new BusinessException("Cliente no registrado."));
                if (!client.isActive()) throw new BusinessException("El cliente está inactivo.");

                Vehicle vehicle = vehicles.findById(order.getVehicleId())
                        .orElseThrow(() -> new BusinessException("Vehículo no registrado."));
                if (vehicle.getClientId() != order.getClientId()) {
                    throw new BusinessException("El vehículo no pertenece al cliente seleccionado.");
                }

                // Validamos y reservamos/descontamos el stock dentro de la misma transacción.
                for (OrderPart item : order.getParts()) {
                    SparePart part = parts.findById(item.sparePartId())
                            .orElseThrow(() -> new BusinessException("Repuesto no encontrado: " + item.sparePartId()));
                    if (!part.isActive()) throw new StockException("El repuesto está inactivo: " + part.getReferenceCode());
                    if (item.quantity() > part.getStockAvailable()) {
                        throw new StockException("Stock insuficiente para " + part.getReferenceCode()
                                + ". Disponible: " + part.getStockAvailable() + ", solicitado: " + item.quantity());
                    }
                }

                order.setEntryDate(LocalDateTime.now());
                order.setStatus(OrderStatus.ABIERTA);
                order.setFinalCost(calculateTotal(order));
                int id = orders.insertOrder(c, order);

                for (OrderPart item : order.getParts()) {
                    parts.decreaseStock(c, item.sparePartId(), item.quantity());
                    orders.insertPart(c, id, item);
                    try (var movement = c.prepareStatement(
                            "INSERT INTO inventory_movements(spare_part_id,order_id,movement_type,quantity,note) VALUES(?,?,?,?,?)")) {
                        movement.setInt(1, item.sparePartId());
                        movement.setInt(2, id);
                        movement.setString(3, "CONSUMO");
                        movement.setInt(4, item.quantity());
                        movement.setString(5, "Stock reservado/consumido por orden de servicio");
                        movement.executeUpdate();
                    }
                }

                c.commit();
                HttpLog.log("POST", "/ordenes", String.valueOf(id));
                return id;
            } catch (Exception e) {
                c.rollback();
                if (e instanceof BusinessException be) throw be;
                throw e;
            } finally {
                c.setAutoCommit(true);
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("No se pudo registrar la orden. Se hizo rollback de la transacción.", e);
        }
    }

    @Override
    public void updateStatus(int id, OrderStatus status, BigDecimal cost, String diagnosis) throws BusinessException {
        if (status == null) throw new BusinessException("El estado de la orden es obligatorio.");
        if (cost == null || cost.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("El costo final no puede ser negativo.");
        }

        try (Connection c = Db.getConnection()) {
            c.setAutoCommit(false);
            try {
                ServiceOrder order = orders.findById(id)
                        .orElseThrow(() -> new BusinessException("Orden no encontrada."));

                if (order.getStatus() == OrderStatus.FINALIZADA && status != OrderStatus.FINALIZADA) {
                    throw new BusinessException("Una orden finalizada no puede volver a un estado anterior.");
                }

                orders.updateStatusAndCost(c, id, status, cost, diagnosis);

                if (status == OrderStatus.FINALIZADA) {
                    // Actualiza el registro de inventario y reconcilia stock dentro de la misma transacción.
                    parts.confirmInventoryForOrder(c, id);
                } else if (status == OrderStatus.CANCELADA) {
                    // Si se cancela, el stock reservado al crear la orden se devuelve.
                    parts.cancelInventoryForOrder(c, id);
                }

                c.commit();
                HttpLog.log("PATCH", "/ordenes/" + id, "estado=" + status + ", costo=" + cost);
            } catch (Exception e) {
                c.rollback();
                if (e instanceof BusinessException be) throw be;
                throw e;
            } finally {
                c.setAutoCommit(true);
            }
        } catch (BusinessException e) {
            throw e;
        } catch (SQLException e) {
            throw new BusinessException("No se pudo actualizar la orden. Se hizo rollback.", e);
        }
    }

    @Override public Optional<ServiceOrder> find(int id) throws BusinessException {
        try { HttpLog.log("GET", "/ordenes/" + id, ""); return orders.findById(id); }
        catch (SQLException e) { throw new BusinessException("No se pudo consultar la orden.", e); }
    }

    @Override public List<ServiceOrder> historyByVehicle(int vehicleId) throws BusinessException {
        try { HttpLog.log("GET", "/vehiculos/" + vehicleId + "/ordenes", ""); return orders.findByVehicle(vehicleId); }
        catch (SQLException e) { throw new BusinessException("No se pudo consultar historial.", e); }
    }

    @Override public BigDecimal calculateTotal(ServiceOrder order) {
        return order.getParts().stream().map(OrderPart::total).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void validate(ServiceOrder order) throws BusinessException {
        if (order == null) throw new BusinessException("La orden es obligatoria.");
        if (order.getClientId() <= 0) throw new BusinessException("Debe seleccionar un cliente válido.");
        if (order.getVehicleId() <= 0) throw new BusinessException("Debe seleccionar un vehículo válido.");
        if (order.getMechanicName() == null || order.getMechanicName().isBlank()) {
            throw new BusinessException("El mecánico es obligatorio.");
        }
        if (order.getProblemDescription() == null || order.getProblemDescription().isBlank()) {
            throw new BusinessException("La descripción del problema es obligatoria.");
        }
        if (order.getParts() == null) throw new BusinessException("La lista de repuestos es inválida.");
        for (OrderPart item : order.getParts()) {
            if (item == null || item.quantity() <= 0) {
                throw new BusinessException("La cantidad del repuesto debe ser mayor a cero.");
            }
            if (item.unitPrice() == null || item.unitPrice().compareTo(BigDecimal.ZERO) < 0) {
                throw new BusinessException("El precio del repuesto no puede ser negativo.");
            }
        }
    }
}
