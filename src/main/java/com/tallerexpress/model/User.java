package com.tallerexpress.model;
import java.time.LocalDateTime;
public class User {
    private int id; private String username, passwordHash, fullName; private Role role; private UserStatus status; private LocalDateTime createdAt;
    public User() {}
    public User(int id,String username,String passwordHash,String fullName,Role role,UserStatus status,LocalDateTime createdAt){this.id=id;this.username=username;this.passwordHash=passwordHash;this.fullName=fullName;this.role=role;this.status=status;this.createdAt=createdAt;}
    public int getId(){return id;} public void setId(int v){id=v;} public String getUsername(){return username;} public void setUsername(String v){username=v;} public String getPasswordHash(){return passwordHash;} public void setPasswordHash(String v){passwordHash=v;} public String getFullName(){return fullName;} public void setFullName(String v){fullName=v;} public Role getRole(){return role;} public void setRole(Role v){role=v;} public UserStatus getStatus(){return status;} public void setStatus(UserStatus v){status=v;} public LocalDateTime getCreatedAt(){return createdAt;} public void setCreatedAt(LocalDateTime v){createdAt=v;}
}
