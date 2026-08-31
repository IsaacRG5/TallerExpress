package com.tallerexpress.model;
import java.time.LocalDateTime;
public class Client {
    private int id; private String document,fullName,phone,email,address; private boolean active; private LocalDateTime createdAt;
    public Client() {} public Client(int id,String document,String fullName,String phone,String email,String address,boolean active,LocalDateTime createdAt){this.id=id;this.document=document;this.fullName=fullName;this.phone=phone;this.email=email;this.address=address;this.active=active;this.createdAt=createdAt;}
    public int getId(){return id;} public void setId(int v){id=v;} public String getDocument(){return document;} public void setDocument(String v){document=v;} public String getFullName(){return fullName;} public void setFullName(String v){fullName=v;} public String getPhone(){return phone;} public void setPhone(String v){phone=v;} public String getEmail(){return email;} public void setEmail(String v){email=v;} public String getAddress(){return address;} public void setAddress(String v){address=v;} public boolean isActive(){return active;} public void setActive(boolean v){active=v;} public LocalDateTime getCreatedAt(){return createdAt;} public void setCreatedAt(LocalDateTime v){createdAt=v;}
}
