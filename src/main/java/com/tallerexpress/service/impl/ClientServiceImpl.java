package com.tallerexpress.service.impl;

import com.tallerexpress.repository.ClientDao;
import com.tallerexpress.exception.BusinessException;
import com.tallerexpress.model.Client;
import com.tallerexpress.service.ClientService;
import com.tallerexpress.config.HttpLog;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class ClientServiceImpl implements ClientService {
    private final ClientDao dao;
    public ClientServiceImpl(ClientDao dao) { this.dao = dao; }

    @Override public int create(Client c) throws BusinessException {
        validate(c);
        try { int id = dao.insert(c); HttpLog.log("POST", "/clientes", c.getDocument()); return id; }
        catch (SQLException e) { throw new BusinessException("No se pudo registrar el cliente. Verifique que el documento no esté duplicado.", e); }
    }

    @Override public void update(Client c) throws BusinessException {
        validate(c);
        try { dao.update(c); HttpLog.log("PATCH", "/clientes/" + c.getId(), c.getDocument()); }
        catch (SQLException e) { throw new BusinessException("No se pudo actualizar el cliente.", e); }
    }

    @Override public void delete(int id) throws BusinessException {
        try { dao.delete(id); HttpLog.log("DELETE", "/clientes/" + id, ""); }
        catch (SQLException e) { throw new BusinessException("No se puede eliminar el cliente si tiene vehículos u órdenes asociadas.", e); }
    }

    @Override public List<Client> list() throws BusinessException {
        try { HttpLog.log("GET", "/clientes", ""); return dao.findAll(); }
        catch (SQLException e) { throw new BusinessException("No se pudieron listar clientes.", e); }
    }

    @Override public Optional<Client> find(int id) throws BusinessException {
        try { HttpLog.log("GET", "/clientes/" + id, ""); return dao.findById(id); }
        catch (SQLException e) { throw new BusinessException("No se pudo consultar el cliente.", e); }
    }

    private void validate(Client c) throws BusinessException {
        if (c == null) throw new BusinessException("El cliente es obligatorio.");
        if (c.getDocument() == null || c.getDocument().isBlank()) throw new BusinessException("El documento es obligatorio.");
        if (c.getFullName() == null || c.getFullName().isBlank()) throw new BusinessException("El nombre completo es obligatorio.");
        if (c.getPhone() == null || c.getPhone().isBlank()) throw new BusinessException("El teléfono es obligatorio.");
    }
}
