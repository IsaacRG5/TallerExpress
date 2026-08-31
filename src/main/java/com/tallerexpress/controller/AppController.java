package com.tallerexpress.controller;

import com.tallerexpress.repository.impl.ClientDaoJdbc;
import com.tallerexpress.repository.impl.ServiceOrderDaoJdbc;
import com.tallerexpress.repository.impl.SparePartDaoJdbc;
import com.tallerexpress.repository.impl.UserDaoJdbc;
import com.tallerexpress.repository.impl.VehicleDaoJdbc;
import com.tallerexpress.service.DefaultUserPropertiesDecorator;
import com.tallerexpress.exception.BusinessException;
import com.tallerexpress.model.*;
import com.tallerexpress.service.*;
import com.tallerexpress.service.impl.*;
import com.tallerexpress.view.TablePrinter;
import javax.swing.*;
import java.math.BigDecimal;
import java.util.*;

public class AppController {
    private final ClientDaoJdbc clientDao = new ClientDaoJdbc();
    private final VehicleDaoJdbc vehicleDao = new VehicleDaoJdbc();
    private final SparePartDaoJdbc spareDao = new SparePartDaoJdbc();

    private final ClientService clientService = new ClientServiceImpl(clientDao);
    private final VehicleService vehicleService = new VehicleServiceImpl(vehicleDao, clientDao);
    private final SparePartService spareService = new SparePartServiceImpl(spareDao);
    private final UserService userService = new UserServiceImpl(new UserDaoJdbc());
    private final UserCreator decoratedUserCreator = new DefaultUserPropertiesDecorator(userService);
    private final ServiceOrderService orderService = new ServiceOrderServiceImpl(
            new ServiceOrderDaoJdbc(), clientDao, vehicleDao, spareDao);

    private User session;

    public void start() {
        while (true) {
            if (!login()) return;
            mainMenu();
        }
    }

    private boolean login() {
        JPanel panel = new JPanel();
        JTextField username = new JTextField(18);
        JPasswordField password = new JPasswordField(18);
        panel.add(new JLabel("Usuario:")); panel.add(username);
        panel.add(new JLabel("Contraseña:")); panel.add(password);

        int result = JOptionPane.showConfirmDialog(null, panel, "TallerExpress | Acceso",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return false;

        try {
            session = userService.login(username.getText().trim(), new String(password.getPassword())).orElseThrow();
            JOptionPane.showMessageDialog(null,
                    "Bienvenido, " + session.getFullName() + "\nRol: " + session.getRole(),
                    "Acceso correcto", JOptionPane.INFORMATION_MESSAGE);
            return true;
        } catch (BusinessException e) {
            error(e.getMessage());
            return false;
        }
    }

    private void mainMenu() {
        String[] options = {"Repuestos", "Clientes", "Vehículos", "Usuarios", "Órdenes de Servicio", "Cerrar sesión"};
        while (session != null) {
            int option = JOptionPane.showOptionDialog(null, "Seleccione una opción del sistema",
                    "TallerExpress | " + session.getRole(), JOptionPane.DEFAULT_OPTION,
                    JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
            if (option < 0 || option == 5) { session = null; return; }
            try {
                switch (option) {
                    case 0 -> spareMenu();
                    case 1 -> clientMenu();
                    case 2 -> vehicleMenu();
                    case 3 -> userMenu();
                    case 4 -> orderMenu();
                    default -> { }
                }
            } catch (BusinessException e) { error(e.getMessage()); }
        }
    }

    private void spareMenu() throws BusinessException {
        String[] options = {"Registrar", "Editar", "Listar/filtrar", "Activar/Desactivar", "Eliminar", "Volver"};
        while (true) {
            int option = JOptionPane.showOptionDialog(null, "Administración de repuestos", "Repuestos",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
            if (option < 0 || option == 5) return;
            try {
                switch (option) {
                    case 0 -> createSpare();
                    case 1 -> editSpare();
                    case 2 -> listSpare();
                    case 3 -> toggleSpare();
                    case 4 -> deleteSpare();
                    default -> { }
                }
            } catch (BusinessException e) { error(e.getMessage()); }
        }
    }

    private void createSpare() throws BusinessException {
        String code = req("Código de referencia");
        String name = req("Nombre");
        String category = req("Categoría");
        String supplier = req("Proveedor");
        int total = positiveOrZero("Stock total");
        int available = positiveOrZero("Stock disponible");
        BigDecimal price = money("Precio unitario");
        SparePart part = new SparePart(0, code, name, category, supplier, total, available, price, true, null);
        int id = spareService.create(part);
        ok("Repuesto registrado. ID: " + id);
    }

    private void editSpare() throws BusinessException {
        int id = positive("ID del repuesto");
        SparePart old = spareService.list(null, null).stream().filter(p -> p.getId() == id).findFirst()
                .orElseThrow(() -> new BusinessException("Repuesto no encontrado."));
        old.setName(req("Nombre", old.getName()));
        old.setCategory(req("Categoría", old.getCategory()));
        old.setSupplier(req("Proveedor", old.getSupplier()));
        old.setStockTotal(positiveOrZero("Stock total", old.getStockTotal()));
        old.setStockAvailable(positiveOrZero("Stock disponible", old.getStockAvailable()));
        old.setUnitPrice(money("Precio unitario", old.getUnitPrice()));
        spareService.update(old);
        ok("Repuesto actualizado.");
    }

    private void listSpare() throws BusinessException {
        String category = opt("Categoría (vacío = todas)");
        String supplier = opt("Proveedor (vacío = todos)");
        List<SparePart> parts = spareService.list(category, supplier);
        List<String[]> rows = new ArrayList<>();
        for (SparePart p : parts) rows.add(new String[]{String.valueOf(p.getId()), p.getReferenceCode(), p.getName(),
                p.getCategory(), p.getSupplier(), p.getStockAvailable() + "/" + p.getStockTotal(),
                p.getUnitPrice().toString(), p.isActive() ? "[ACTIVO]" : "[INACTIVO]"});
        show(TablePrinter.print(new String[]{"ID","REF","NOMBRE","CATEG.","PROVEEDOR","DISP/TOTAL","PRECIO","ESTADO"}, rows), "Listado de repuestos");
    }

    private void toggleSpare() throws BusinessException { int id = positive("ID"); spareService.toggle(id); ok("Estado actualizado."); }

    private void deleteSpare() throws BusinessException {
        int id = positive("ID del repuesto");
        if (JOptionPane.showConfirmDialog(null, "¿Eliminar repuesto?", "Confirmar", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        spareService.delete(id);
        ok("Repuesto eliminado.");
    }

    private void clientMenu() throws BusinessException {
        String[] options = {"Registrar", "Editar", "Listar", "Eliminar", "Volver"};
        while (true) {
            int option = JOptionPane.showOptionDialog(null, "Administración de clientes", "Clientes",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
            if (option < 0 || option == 4) return;
            try { switch (option) { case 0 -> createClient(); case 1 -> editClient(); case 2 -> listClients(); case 3 -> deleteClient(); default -> { } } }
            catch (BusinessException e) { error(e.getMessage()); }
        }
    }

    private void createClient() throws BusinessException {
        Client c = new Client(0, req("Documento"), req("Nombre completo"), req("Teléfono"),
                opt("Correo"), opt("Dirección"), true, null);
        int id = clientService.create(c); ok("Cliente registrado. ID: " + id);
    }

    private void editClient() throws BusinessException {
        int id = positive("ID cliente");
        Client c = clientService.find(id).orElseThrow(() -> new BusinessException("Cliente no encontrado."));
        c.setFullName(req("Nombre", c.getFullName()));
        c.setPhone(req("Teléfono", c.getPhone()));
        c.setEmail(opt("Correo", c.getEmail()));
        c.setAddress(opt("Dirección", c.getAddress()));
        c.setActive(JOptionPane.showConfirmDialog(null, "¿Cliente activo?", "Estado", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION);
        clientService.update(c); ok("Cliente actualizado.");
    }

    private void listClients() throws BusinessException {
        List<String[]> rows = new ArrayList<>();
        for (Client c : clientService.list()) rows.add(new String[]{String.valueOf(c.getId()), c.getDocument(), c.getFullName(), c.getPhone(), c.isActive() ? "[ACTIVO]" : "[INACTIVO]"});
        show(TablePrinter.print(new String[]{"ID","DOC","NOMBRE","TELÉFONO","ESTADO"}, rows), "Clientes");
    }

    private void deleteClient() throws BusinessException {
        int id = positive("ID cliente");
        if (JOptionPane.showConfirmDialog(null, "¿Eliminar cliente?", "Confirmar", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        clientService.delete(id); ok("Cliente eliminado.");
    }

    private void vehicleMenu() throws BusinessException {
        String[] options = {"Registrar", "Editar", "Listar", "Historial por cliente", "Eliminar", "Volver"};
        while (true) {
            int option = JOptionPane.showOptionDialog(null, "Administración de vehículos", "Vehículos",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
            if (option < 0 || option == 5) return;
            try { switch (option) { case 0 -> createVehicle(); case 1 -> editVehicle(); case 2 -> listVehicles(); case 3 -> historyVehicles(); case 4 -> deleteVehicle(); default -> { } } }
            catch (BusinessException e) { error(e.getMessage()); }
        }
    }

    private void createVehicle() throws BusinessException {
        int clientId = positive("ID cliente");
        Vehicle v = new Vehicle(0, clientId, req("Placa").toUpperCase(), req("Marca"), req("Modelo"), year("Año"), null);
        int id = vehicleService.create(v); ok("Vehículo registrado. ID: " + id);
    }

    private void editVehicle() throws BusinessException {
        int id = positive("ID vehículo");
        Vehicle v = vehicleService.find(id).orElseThrow(() -> new BusinessException("Vehículo no encontrado."));
        v.setClientId(positive("ID cliente", v.getClientId()));
        v.setPlate(req("Placa", v.getPlate()).toUpperCase());
        v.setBrand(req("Marca", v.getBrand())); v.setModel(req("Modelo", v.getModel())); v.setYear(year("Año", v.getYear()));
        vehicleService.update(v); ok("Vehículo actualizado.");
    }

    private void listVehicles() throws BusinessException {
        List<String[]> rows = new ArrayList<>();
        for (Vehicle v : vehicleService.list()) rows.add(new String[]{String.valueOf(v.getId()), String.valueOf(v.getClientId()), v.getPlate(), v.getBrand(), v.getModel(), String.valueOf(v.getYear())});
        show(TablePrinter.print(new String[]{"ID","CLIENTE","PLACA","MARCA","MODELO","AÑO"}, rows), "Vehículos");
    }

    private void historyVehicles() throws BusinessException {
        int clientId = positive("ID cliente");
        List<String[]> rows = new ArrayList<>();
        for (Vehicle v : vehicleService.byClient(clientId)) rows.add(new String[]{String.valueOf(v.getId()), v.getPlate(), v.getBrand(), v.getModel(), String.valueOf(v.getYear())});
        show(TablePrinter.print(new String[]{"ID","PLACA","MARCA","MODELO","AÑO"}, rows), "Vehículos del cliente " + clientId);
    }

    private void deleteVehicle() throws BusinessException {
        int id = positive("ID vehículo");
        if (JOptionPane.showConfirmDialog(null, "¿Eliminar vehículo?", "Confirmar", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        vehicleService.delete(id); ok("Vehículo eliminado.");
    }

    private void userMenu() throws BusinessException {
        if (session.getRole() != Role.ADMIN) { error("Solo ADMIN puede gestionar usuarios."); return; }
        String[] options = {"Registrar recepcionista", "Editar", "Eliminar", "Listar", "Volver"};
        while (true) {
            int option = JOptionPane.showOptionDialog(null, "Administración de usuarios", "Usuarios", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
            if (option < 0 || option == 4) return;
            try { switch (option) { case 0 -> createUser(); case 1 -> editUser(); case 2 -> deleteUser(); case 3 -> listUsers(); default -> { } } }
            catch (BusinessException e) { error(e.getMessage()); }
        }
    }

    private void createUser() throws BusinessException {
        User user = new User(); user.setUsername(req("Usuario")); user.setFullName(req("Nombre completo"));
        String password = req("Contraseña");
        int id = decoratedUserCreator.create(user, password);
        ok("Usuario creado con propiedades por defecto: RECEPCIONISTA / ACTIVO. ID: " + id);
    }

    private void editUser() throws BusinessException {
        int id = positive("ID usuario");
        User user = userService.list().stream().filter(x -> x.getId() == id).findFirst().orElseThrow(() -> new BusinessException("Usuario no encontrado."));
        user.setFullName(req("Nombre", user.getFullName()));
        String role = req("Rol: ADMIN o RECEPCIONISTA", user.getRole().name()).toUpperCase();
        try { user.setRole(Role.valueOf(role)); } catch (IllegalArgumentException e) { throw new BusinessException("Rol inválido. Use ADMIN o RECEPCIONISTA."); }
        user.setStatus(JOptionPane.showConfirmDialog(null, "¿Usuario activo?", "Estado", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION ? UserStatus.ACTIVO : UserStatus.INACTIVO);
        userService.update(user); ok("Usuario actualizado.");
    }

    private void deleteUser() throws BusinessException {
        int id = positive("ID usuario");
        if (id == session.getId()) { error("No puedes eliminar el usuario con el que estás conectado."); return; }
        if (JOptionPane.showConfirmDialog(null, "¿Eliminar usuario?", "Confirmar", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) { userService.delete(id); ok("Usuario eliminado."); }
    }

    private void listUsers() throws BusinessException {
        List<String[]> rows = new ArrayList<>();
        for (User u : userService.list()) rows.add(new String[]{String.valueOf(u.getId()), u.getUsername(), u.getFullName(), u.getRole().name(), u.getStatus().name()});
        show(TablePrinter.print(new String[]{"ID","USUARIO","NOMBRE","ROL","ESTADO"}, rows), "Usuarios");
    }

    private void orderMenu() throws BusinessException {
        String[] options = {"Registrar orden", "Actualizar estado/finalizar", "Consultar orden", "Historial por vehículo", "Volver"};
        while (true) {
            int option = JOptionPane.showOptionDialog(null, "Gestión de Órdenes de Servicio", "Órdenes", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
            if (option < 0 || option == 4) return;
            try { switch (option) { case 0 -> createOrder(); case 1 -> updateOrder(); case 2 -> viewOrder(); case 3 -> historyOrders(); default -> { } } }
            catch (BusinessException e) { error(e.getMessage()); }
        }
    }

    private void createOrder() throws BusinessException {
        int clientId = positive("ID cliente"); int vehicleId = positive("ID vehículo");
        String mechanic = req("Mecánico responsable"); String problem = req("Descripción del problema"); String diagnosis = opt("Diagnóstico inicial");
        List<OrderPart> items = new ArrayList<>(); Set<Integer> selectedPartIds = new HashSet<>();
        while (JOptionPane.showConfirmDialog(null, "¿Agregar repuesto?", "Repuestos", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            int partId = positive("ID repuesto");
            if (!selectedPartIds.add(partId)) throw new BusinessException("No puedes agregar el mismo repuesto dos veces a una orden.");
            int quantity = positive("Cantidad");
            SparePart part = spareService.list(null, null).stream().filter(x -> x.getId() == partId).findFirst().orElseThrow(() -> new BusinessException("Repuesto no encontrado."));
            items.add(new OrderPart(partId, part.getReferenceCode(), part.getName(), quantity, part.getUnitPrice()));
        }
        ServiceOrder order = new ServiceOrder(); order.setClientId(clientId); order.setVehicleId(vehicleId); order.setMechanicName(mechanic);
        order.setProblemDescription(problem); order.setDiagnosis(diagnosis); order.setParts(items);
        BigDecimal total = orderService.calculateTotal(order);
        if (JOptionPane.showConfirmDialog(null, "Costo calculado: $" + total + "\n¿Registrar orden?", "Confirmar", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        int id = orderService.create(order); ok("Orden registrada. ID: " + id + "\nCosto calculado: $" + total);
    }

    private void updateOrder() throws BusinessException {
        int id = positive("ID orden");
        String state = req("Estado: ABIERTA, EN_PROCESO, FINALIZADA o CANCELADA", "EN_PROCESO").toUpperCase();
        OrderStatus status;
        try { status = OrderStatus.valueOf(state); } catch (IllegalArgumentException e) { throw new BusinessException("Estado inválido."); }
        BigDecimal cost = money("Costo final"); String diagnosis = opt("Diagnóstico final");
        orderService.updateStatus(id, status, cost, diagnosis); ok("Orden actualizada y procesada transaccionalmente.");
    }

    private void viewOrder() throws BusinessException {
        int id = positive("ID orden"); ServiceOrder order = orderService.find(id).orElseThrow(() -> new BusinessException("Orden no encontrada."));
        show(orderText(order), "Orden #" + id);
    }

    private void historyOrders() throws BusinessException {
        int vehicleId = positive("ID vehículo"); List<ServiceOrder> orders = orderService.historyByVehicle(vehicleId);
        if (orders.isEmpty()) { show("Sin servicios registrados.", "Historial"); return; }
        StringBuilder text = new StringBuilder(); for (ServiceOrder order : orders) text.append(orderText(order)).append("\n\n");
        show(text.toString(), "Historial del vehículo " + vehicleId);
    }

    private String orderText(ServiceOrder order) {
        StringBuilder text = new StringBuilder();
        text.append("Orden #").append(order.getId()).append(" | Estado: ").append(order.getStatus()).append("\n")
                .append("Cliente: ").append(order.getClientId()).append(" | Vehículo: ").append(order.getVehicleId()).append("\n")
                .append("Mecánico: ").append(order.getMechanicName()).append(" | Fecha: ").append(order.getEntryDate()).append("\n")
                .append("Problema: ").append(order.getProblemDescription()).append("\n")
                .append("Diagnóstico: ").append(order.getDiagnosis()).append("\nRepuestos:\n");
        for (OrderPart part : order.getParts()) text.append(" - ").append(part.name()).append(" x").append(part.quantity()).append(" = $").append(part.total()).append("\n");
        text.append("Costo total: $").append(order.getFinalCost());
        return text.toString();
    }

    private String req(String label) { return req(label, null); }
    private String req(String label, String defaultValue) {
        while (true) {
            String value = JOptionPane.showInputDialog(null, label, defaultValue);
            if (value == null) throw new IllegalStateException("Operación cancelada.");
            value = value.trim(); if (!value.isEmpty()) return value;
            error("El campo es obligatorio.");
        }
    }
    private String opt(String label) { return opt(label, null); }
    private String opt(String label, String defaultValue) { String value = JOptionPane.showInputDialog(null, label, defaultValue); return value == null ? "" : value.trim(); }
    private int positive(String label) { return positive(label, null); }
    private int positive(String label, Integer defaultValue) { int value = integer(label, defaultValue); if (value <= 0) throw new IllegalStateException("Debe ingresar un número mayor que cero."); return value; }
    private int positiveOrZero(String label) { return positiveOrZero(label, null); }
    private int positiveOrZero(String label, Integer defaultValue) { int value = integer(label, defaultValue); if (value < 0) throw new IllegalStateException("El valor no puede ser negativo."); return value; }
    private int year(String label) { return year(label, null); }
    private int year(String label, Integer defaultValue) { int value = integer(label, defaultValue); if (value < 1886 || value > 2100) throw new IllegalStateException("El año debe estar entre 1886 y 2100."); return value; }
    private int integer(String label, Integer defaultValue) { while (true) { try { return Integer.parseInt(req(label, defaultValue == null ? null : String.valueOf(defaultValue))); } catch (NumberFormatException e) { error("Debe ingresar un número entero válido."); } } }
    private BigDecimal money(String label) { return money(label, null); }
    private BigDecimal money(String label, BigDecimal defaultValue) { while (true) { try { BigDecimal value = new BigDecimal(req(label, defaultValue == null ? null : defaultValue.toString())); if (value.compareTo(BigDecimal.ZERO) < 0) { error("El valor no puede ser negativo."); continue; } return value; } catch (NumberFormatException e) { error("Debe ingresar un valor numérico válido."); } } }
    private void ok(String message) { JOptionPane.showMessageDialog(null, message, "Éxito", JOptionPane.INFORMATION_MESSAGE); }
    private void error(String message) { JOptionPane.showMessageDialog(null, message == null ? "Error inesperado." : message, "Error", JOptionPane.ERROR_MESSAGE); }
    private void show(String text, String title) { JTextArea area = new JTextArea(text, 25, 100); area.setEditable(false); JOptionPane.showMessageDialog(null, new JScrollPane(area), title, JOptionPane.INFORMATION_MESSAGE); }
}
