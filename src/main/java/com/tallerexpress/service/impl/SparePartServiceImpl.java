package com.tallerexpress.service.impl;

import com.tallerexpress.repository.SparePartDao;
import com.tallerexpress.exception.BusinessException;
import com.tallerexpress.exception.DuplicateException;
import com.tallerexpress.model.SparePart;
import com.tallerexpress.service.SparePartService;
import com.tallerexpress.config.HttpLog;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class SparePartServiceImpl implements SparePartService {
    private final SparePartDao dao;
    public SparePartServiceImpl(SparePartDao dao) { this.dao = dao; }

    @Override public int create(SparePart p) throws BusinessException {
        normalize(p); validate(p);
        try {
            if (dao.findByReference(p.getReferenceCode()).isPresent()) throw new DuplicateException("El código de referencia ya existe.");
            int id = dao.insert(p); HttpLog.log("POST", "/repuestos", p.getReferenceCode()); return id;
        } catch (BusinessException e) { throw e; }
        catch (SQLException e) { throw new BusinessException("No se pudo registrar el repuesto.", e); }
    }

    @Override public void update(SparePart p) throws BusinessException {
        normalize(p); validate(p);
        try { dao.update(p); HttpLog.log("PATCH", "/repuestos/" + p.getId(), p.getReferenceCode()); }
        catch (SQLException e) { throw new BusinessException("No se pudo actualizar el repuesto.", e); }
    }

    @Override public void toggle(int id) throws BusinessException {
        try {
            SparePart p = dao.findById(id).orElseThrow(() -> new BusinessException("Repuesto no encontrado."));
            boolean newState = !p.isActive(); dao.setActive(id, newState);
            HttpLog.log("PATCH", "/repuestos/" + id, "activo=" + newState);
        } catch (BusinessException e) { throw e; }
        catch (SQLException e) { throw new BusinessException("No se pudo cambiar el estado.", e); }
    }

    @Override public void delete(int id) throws BusinessException {
        try { dao.delete(id); HttpLog.log("DELETE", "/repuestos/" + id, ""); }
        catch (SQLException e) { throw new BusinessException("No se puede eliminar el repuesto si tiene movimientos o ha sido usado en una orden.", e); }
    }

    @Override public List<SparePart> list(String category, String supplier) throws BusinessException {
        try { HttpLog.log("GET", "/repuestos", safe(category) + "/" + safe(supplier)); return dao.filter(blank(category), blank(supplier)); }
        catch (SQLException e) { throw new BusinessException("No se pudo listar repuestos.", e); }
    }

    private void normalize(SparePart p) throws BusinessException {
        if (p == null) throw new BusinessException("El repuesto es obligatorio.");
        if (p.getReferenceCode() != null) p.setReferenceCode(p.getReferenceCode().trim().toUpperCase());
        if (p.getCategory() != null) p.setCategory(p.getCategory().trim());
        if (p.getSupplier() != null) p.setSupplier(p.getSupplier().trim());
        if (p.getName() != null) p.setName(p.getName().trim());
    }

    private void validate(SparePart p) throws BusinessException {
        if (p.getReferenceCode() == null || p.getReferenceCode().isBlank()) throw new BusinessException("El código es obligatorio.");
        if (p.getName() == null || p.getName().isBlank()) throw new BusinessException("El nombre es obligatorio.");
        if (p.getCategory() == null || p.getCategory().isBlank()) throw new BusinessException("La categoría es obligatoria.");
        if (p.getSupplier() == null || p.getSupplier().isBlank()) throw new BusinessException("El proveedor es obligatorio.");
        if (p.getStockTotal() < 0 || p.getStockAvailable() < 0 || p.getStockAvailable() > p.getStockTotal()) throw new BusinessException("El stock debe ser >= 0 y disponible <= total.");
        if (p.getUnitPrice() == null || p.getUnitPrice().compareTo(BigDecimal.ZERO) < 0) throw new BusinessException("El precio no puede ser negativo.");
    }
    private String blank(String s) { return s == null || s.isBlank() ? null : s.trim(); }
    private String safe(String s) { return s == null ? "" : s.trim(); }
}
