package com.tallerexpress.repository.impl;

import com.tallerexpress.repository.VehicleDao;
import com.tallerexpress.model.Vehicle;
import com.tallerexpress.config.Db;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class VehicleDaoJdbc implements VehicleDao {
    private Vehicle map(ResultSet r) throws SQLException {
        Timestamp created = r.getTimestamp("created_at");
        return new Vehicle(r.getInt("id"), r.getInt("client_id"), r.getString("plate"),
                r.getString("brand"), r.getString("model"), r.getInt("vehicle_year"),
                created == null ? null : created.toLocalDateTime());
    }

    @Override public int insert(Vehicle v) throws SQLException {
        String q="INSERT INTO vehicles(client_id,plate,brand,model,vehicle_year) VALUES(?,?,?,?,?) RETURNING id";
        try(Connection c=Db.getConnection(); PreparedStatement p=c.prepareStatement(q)){
            p.setInt(1,v.getClientId()); p.setString(2,v.getPlate()); p.setString(3,v.getBrand()); p.setString(4,v.getModel()); p.setInt(5,v.getYear());
            try(ResultSet r=p.executeQuery()){r.next(); return r.getInt(1);}
        }
    }
    @Override public Optional<Vehicle> findById(int id)throws SQLException{return one("SELECT * FROM vehicles WHERE id=?",id);}
    @Override public Optional<Vehicle> findByPlate(String plate)throws SQLException{
        try(Connection c=Db.getConnection();PreparedStatement p=c.prepareStatement("SELECT * FROM vehicles WHERE plate=?")){p.setString(1,plate);try(ResultSet r=p.executeQuery()){return r.next()?Optional.of(map(r)):Optional.empty();}}
    }
    @Override public List<Vehicle> findByClient(int id)throws SQLException{return query("SELECT * FROM vehicles WHERE client_id=? ORDER BY id",id);}
    @Override public List<Vehicle> findAll()throws SQLException{
        List<Vehicle> result=new ArrayList<>();
        try(Connection c=Db.getConnection();Statement s=c.createStatement();ResultSet r=s.executeQuery("SELECT * FROM vehicles ORDER BY id")){while(r.next())result.add(map(r));}
        return result;
    }
    @Override public void update(Vehicle v)throws SQLException{
        String q="UPDATE vehicles SET client_id=?,plate=?,brand=?,model=?,vehicle_year=? WHERE id=?";
        try(Connection c=Db.getConnection();PreparedStatement p=c.prepareStatement(q)){p.setInt(1,v.getClientId());p.setString(2,v.getPlate());p.setString(3,v.getBrand());p.setString(4,v.getModel());p.setInt(5,v.getYear());p.setInt(6,v.getId());if(p.executeUpdate()!=1)throw new SQLException("Vehículo no encontrado: "+v.getId());}
    }
    @Override public void delete(int id)throws SQLException{
        try(Connection c=Db.getConnection();PreparedStatement p=c.prepareStatement("DELETE FROM vehicles WHERE id=?")){p.setInt(1,id);if(p.executeUpdate()!=1)throw new SQLException("Vehículo no encontrado: "+id);}
    }
    private Optional<Vehicle> one(String q,int id)throws SQLException{try(Connection c=Db.getConnection();PreparedStatement p=c.prepareStatement(q)){p.setInt(1,id);try(ResultSet r=p.executeQuery()){return r.next()?Optional.of(map(r)):Optional.empty();}}}
    private List<Vehicle> query(String q,int id)throws SQLException{List<Vehicle> l=new ArrayList<>();try(Connection c=Db.getConnection();PreparedStatement p=c.prepareStatement(q)){p.setInt(1,id);try(ResultSet r=p.executeQuery()){while(r.next())l.add(map(r));}}return l;}
}
