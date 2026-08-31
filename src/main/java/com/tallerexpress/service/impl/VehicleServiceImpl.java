package com.tallerexpress.service.impl;

import com.tallerexpress.repository.ClientDao;
import com.tallerexpress.repository.VehicleDao;
import com.tallerexpress.exception.BusinessException;
import com.tallerexpress.exception.DuplicateException;
import com.tallerexpress.model.Vehicle;
import com.tallerexpress.service.VehicleService;
import com.tallerexpress.config.HttpLog;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class VehicleServiceImpl implements VehicleService {
    private final VehicleDao dao;
    private final ClientDao clients;
    public VehicleServiceImpl(VehicleDao dao, ClientDao clients){this.dao=dao;this.clients=clients;}

    @Override public int create(Vehicle v)throws BusinessException{
        validate(v);
        try{
            if(clients.findById(v.getClientId()).isEmpty())throw new BusinessException("El cliente no está registrado.");
            if(dao.findByPlate(v.getPlate()).isPresent())throw new DuplicateException("La placa ya está registrada.");
            int id=dao.insert(v);HttpLog.log("POST","/vehiculos",v.getPlate());return id;
        }catch(BusinessException e){throw e;}catch(SQLException e){throw new BusinessException("No se pudo registrar el vehículo. Verifique la placa y el cliente.",e);}
    }
    @Override public void update(Vehicle v)throws BusinessException{
        validate(v);
        try{
            Vehicle current=dao.findById(v.getId()).orElseThrow(()->new BusinessException("Vehículo no encontrado."));
            if(!current.getPlate().equalsIgnoreCase(v.getPlate()) && dao.findByPlate(v.getPlate()).isPresent())throw new DuplicateException("La placa ya está registrada.");
            if(clients.findById(v.getClientId()).isEmpty())throw new BusinessException("El cliente no está registrado.");
            dao.update(v);HttpLog.log("PATCH","/vehiculos/"+v.getId(),v.getPlate());
        }catch(BusinessException e){throw e;}catch(SQLException e){throw new BusinessException("No se pudo actualizar el vehículo.",e);}
    }
    @Override public void delete(int id)throws BusinessException{try{dao.delete(id);HttpLog.log("DELETE","/vehiculos/"+id,"");}catch(SQLException e){throw new BusinessException("No se puede eliminar el vehículo si tiene órdenes de servicio asociadas.",e);}}
    @Override public List<Vehicle> byClient(int id)throws BusinessException{try{HttpLog.log("GET","/clientes/"+id+"/vehiculos","");return dao.findByClient(id);}catch(SQLException e){throw new BusinessException("No se pudo consultar el historial de vehículos.",e);}}
    @Override public List<Vehicle> list()throws BusinessException{try{HttpLog.log("GET","/vehiculos","");return dao.findAll();}catch(SQLException e){throw new BusinessException("No se pudieron listar vehículos.",e);}}
    @Override public Optional<Vehicle> find(int id)throws BusinessException{try{return dao.findById(id);}catch(SQLException e){throw new BusinessException("No se pudo consultar el vehículo.",e);}}
    private void validate(Vehicle v)throws BusinessException{if(v==null)throw new BusinessException("El vehículo es obligatorio.");if(v.getPlate()==null||v.getPlate().isBlank())throw new BusinessException("La placa es obligatoria.");if(v.getBrand()==null||v.getBrand().isBlank())throw new BusinessException("La marca es obligatoria.");if(v.getModel()==null||v.getModel().isBlank())throw new BusinessException("El modelo es obligatorio.");if(v.getYear()<1886||v.getYear()>2100)throw new BusinessException("El año del vehículo no es válido.");}
}
