package com.tallerexpress.repository;

import com.tallerexpress.model.Vehicle;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface VehicleDao {
    int insert(Vehicle vehicle) throws SQLException;
    Optional<Vehicle> findById(int id) throws SQLException;
    Optional<Vehicle> findByPlate(String plate) throws SQLException;
    List<Vehicle> findByClient(int clientId) throws SQLException;
    List<Vehicle> findAll() throws SQLException;
    void update(Vehicle vehicle) throws SQLException;
    void delete(int id) throws SQLException;
}
