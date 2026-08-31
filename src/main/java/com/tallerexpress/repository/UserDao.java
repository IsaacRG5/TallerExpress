package com.tallerexpress.repository;
import com.tallerexpress.model.*; import java.sql.*; import java.util.*;
public interface UserDao { Optional<User> findByUsername(String username)throws SQLException; Optional<User> findById(int id)throws SQLException; List<User> findAll()throws SQLException; int insert(User u,Connection c)throws SQLException; void update(User u)throws SQLException; void delete(int id)throws SQLException; }
