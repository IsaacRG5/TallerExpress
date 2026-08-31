package com.tallerexpress.repository.impl;

import com.tallerexpress.repository.SparePartDao;
import com.tallerexpress.model.SparePart;
import com.tallerexpress.config.Db;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SparePartDaoJdbc implements SparePartDao {
    private SparePart map(ResultSet r) throws SQLException {
        return new SparePart(r.getInt("id"), r.getString("reference_code"), r.getString("name"),
                r.getString("category"), r.getString("supplier"), r.getInt("stock_total"),
                r.getInt("stock_available"), r.getBigDecimal("unit_price"), r.getBoolean("is_active"),
                r.getTimestamp("created_at").toLocalDateTime());
    }

    @Override
    public int insert(SparePart p) throws SQLException {
        String q = "INSERT INTO spare_parts(reference_code,name,category,supplier,stock_total,stock_available,unit_price,is_active,created_at) VALUES(?,?,?,?,?,?,?,?,CURRENT_TIMESTAMP) RETURNING id";
        try (Connection c = Db.getConnection(); PreparedStatement s = c.prepareStatement(q)) {
            s.setString(1, p.getReferenceCode()); s.setString(2, p.getName()); s.setString(3, p.getCategory());
            s.setString(4, p.getSupplier()); s.setInt(5, p.getStockTotal()); s.setInt(6, p.getStockAvailable());
            s.setBigDecimal(7, p.getUnitPrice()); s.setBoolean(8, p.isActive());
            try (ResultSet r = s.executeQuery()) { r.next(); return r.getInt(1); }
        }
    }

    @Override public Optional<SparePart> findById(int id) throws SQLException { return one("SELECT * FROM spare_parts WHERE id=?", id); }

    @Override
    public Optional<SparePart> findByReference(String code) throws SQLException {
        try (Connection c = Db.getConnection(); PreparedStatement p = c.prepareStatement("SELECT * FROM spare_parts WHERE reference_code=?")) {
            p.setString(1, code);
            try (ResultSet r = p.executeQuery()) { return r.next() ? Optional.of(map(r)) : Optional.empty(); }
        }
    }

    @Override public List<SparePart> findAll() throws SQLException { return filter(null, null); }

    @Override
    public List<SparePart> filter(String cat, String sup) throws SQLException {
        String q = "SELECT * FROM spare_parts WHERE (? IS NULL OR LOWER(category)=LOWER(?)) AND (? IS NULL OR LOWER(supplier)=LOWER(?)) ORDER BY id";
        List<SparePart> result = new ArrayList<>();
        try (Connection c = Db.getConnection(); PreparedStatement p = c.prepareStatement(q)) {
            p.setString(1, cat); p.setString(2, cat); p.setString(3, sup); p.setString(4, sup);
            try (ResultSet r = p.executeQuery()) { while (r.next()) result.add(map(r)); }
        }
        return result;
    }

    private Optional<SparePart> one(String q, int id) throws SQLException {
        try (Connection c = Db.getConnection(); PreparedStatement p = c.prepareStatement(q)) {
            p.setInt(1, id);
            try (ResultSet r = p.executeQuery()) { return r.next() ? Optional.of(map(r)) : Optional.empty(); }
        }
    }

    @Override
    public void update(SparePart p) throws SQLException {
        String q = "UPDATE spare_parts SET name=?,category=?,supplier=?,stock_total=?,stock_available=?,unit_price=?,is_active=? WHERE id=?";
        try (Connection c = Db.getConnection(); PreparedStatement s = c.prepareStatement(q)) {
            s.setString(1, p.getName()); s.setString(2, p.getCategory()); s.setString(3, p.getSupplier());
            s.setInt(4, p.getStockTotal()); s.setInt(5, p.getStockAvailable()); s.setBigDecimal(6, p.getUnitPrice());
            s.setBoolean(7, p.isActive()); s.setInt(8, p.getId());
            if (s.executeUpdate() != 1) throw new SQLException("Repuesto no encontrado: " + p.getId());
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        try (Connection c = Db.getConnection(); PreparedStatement p = c.prepareStatement("DELETE FROM spare_parts WHERE id=?")) {
            p.setInt(1, id);
            if (p.executeUpdate() != 1) throw new SQLException("Repuesto no encontrado: " + id);
        }
    }

    @Override
    public void setActive(int id, boolean active) throws SQLException {
        try (Connection c = Db.getConnection(); PreparedStatement p = c.prepareStatement("UPDATE spare_parts SET is_active=? WHERE id=?")) {
            p.setBoolean(1, active); p.setInt(2, id);
            if (p.executeUpdate() != 1) throw new SQLException("Repuesto no encontrado: " + id);
        }
    }

    @Override
    public void decreaseStock(Connection c, int id, int qty) throws SQLException {
        String q = "UPDATE spare_parts SET stock_available=stock_available-? WHERE id=? AND is_active=TRUE AND stock_available>=?";
        try (PreparedStatement p = c.prepareStatement(q)) {
            p.setInt(1, qty); p.setInt(2, id); p.setInt(3, qty);
            if (p.executeUpdate() != 1) throw new SQLException("Stock insuficiente o repuesto inactivo: " + id);
        }
    }

    @Override
    public void restoreStock(Connection c, int id, int qty) throws SQLException {
        String q = "UPDATE spare_parts SET stock_available=stock_available+? WHERE id=? AND stock_available+?<=stock_total";
        try (PreparedStatement p = c.prepareStatement(q)) {
            p.setInt(1, qty); p.setInt(2, id); p.setInt(3, qty);
            if (p.executeUpdate() != 1) throw new SQLException("No se pudo restaurar el stock del repuesto: " + id);
        }
    }

    @Override
    public void confirmInventoryForOrder(Connection c, int orderId) throws SQLException {
        String movementUpdate = "UPDATE inventory_movements SET movement_type='CONSUMO_CONFIRMADO', note='Consumo confirmado al cerrar la orden' WHERE order_id=? AND movement_type='CONSUMO'";
        try (PreparedStatement p = c.prepareStatement(movementUpdate)) {
            p.setInt(1, orderId); p.executeUpdate();
        }
        String stockReconcile = "UPDATE spare_parts sp SET stock_available = sp.stock_total - COALESCE((SELECT SUM(im.quantity) FROM inventory_movements im WHERE im.spare_part_id=sp.id AND im.movement_type='CONSUMO_CONFIRMADO'),0) WHERE sp.id IN (SELECT spare_part_id FROM inventory_movements WHERE order_id=? AND movement_type='CONSUMO_CONFIRMADO')";
        try (PreparedStatement p = c.prepareStatement(stockReconcile)) { p.setInt(1, orderId); p.executeUpdate(); }
    }

    @Override
    public void cancelInventoryForOrder(Connection c, int orderId) throws SQLException {
        String restore = "UPDATE spare_parts sp SET stock_available = sp.stock_available + COALESCE((SELECT SUM(im.quantity) FROM inventory_movements im WHERE im.order_id=? AND im.spare_part_id=sp.id AND im.movement_type IN ('CONSUMO','CONSUMO_CONFIRMADO')),0) WHERE sp.id IN (SELECT spare_part_id FROM inventory_movements WHERE order_id=? AND movement_type IN ('CONSUMO','CONSUMO_CONFIRMADO'))";
        try (PreparedStatement p = c.prepareStatement(restore)) { p.setInt(1, orderId); p.setInt(2, orderId); p.executeUpdate(); }
        String movementUpdate = "UPDATE inventory_movements SET movement_type='CONSUMO_CANCELADO', note='Consumo revertido por cancelación de la orden' WHERE order_id=? AND movement_type IN ('CONSUMO','CONSUMO_CONFIRMADO')";
        try (PreparedStatement p = c.prepareStatement(movementUpdate)) { p.setInt(1, orderId); p.executeUpdate(); }
    }
}
