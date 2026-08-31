package com.tallerexpress.model;
import java.time.LocalDateTime;
public class Vehicle {
    private int id,clientId,year; private String plate,brand,model; private LocalDateTime createdAt;
    public Vehicle() {} public Vehicle(int id,int clientId,String plate,String brand,String model,int year,LocalDateTime createdAt){this.id=id;this.clientId=clientId;this.plate=plate;this.brand=brand;this.model=model;this.year=year;this.createdAt=createdAt;}
    public int getId(){return id;} public void setId(int v){id=v;} public int getClientId(){return clientId;} public void setClientId(int v){clientId=v;} public String getPlate(){return plate;} public void setPlate(String v){plate=v;} public String getBrand(){return brand;} public void setBrand(String v){brand=v;} public String getModel(){return model;} public void setModel(String v){model=v;} public int getYear(){return year;} public void setYear(int v){year=v;} public LocalDateTime getCreatedAt(){return createdAt;} public void setCreatedAt(LocalDateTime v){createdAt=v;}
}
