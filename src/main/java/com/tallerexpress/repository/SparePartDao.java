package com.tallerexpress.repository;

import com.tallerexpress.model.SparePart;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface SparePartDao {
    int insert(SparePart part) throws SQLException;
    Optional<SparePart> findById(int id) throws SQLException;
    Optional<SparePart> findByReference(String code) throws SQLException;
    List<SparePart> findAll() throws SQLException;
    List<SparePart> filter(String category, String supplier) throws SQLException;
    void update(SparePart part) throws SQLException;
    void setActive(int id, boolean active) throws SQLException;
    void delete(int id) throws SQLException;
    void decreaseStock(Connection connection, int id, int quantity) throws SQLException;
    void restoreStock(Connection connection, int id, int quantity) throws SQLException;
    void confirmInventoryForOrder(Connection connection, int orderId) throws SQLException;
    void cancelInventoryForOrder(Connection connection, int orderId) throws SQLException;
}
