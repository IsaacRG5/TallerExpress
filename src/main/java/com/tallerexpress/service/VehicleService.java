package com.tallerexpress.service;

import com.tallerexpress.exception.BusinessException;
import com.tallerexpress.model.Vehicle;
import java.util.List;
import java.util.Optional;

public interface VehicleService {
    int create(Vehicle vehicle) throws BusinessException;
    void update(Vehicle vehicle) throws BusinessException;
    void delete(int id) throws BusinessException;
    List<Vehicle> byClient(int clientId) throws BusinessException;
    List<Vehicle> list() throws BusinessException;
    Optional<Vehicle> find(int id) throws BusinessException;
}
