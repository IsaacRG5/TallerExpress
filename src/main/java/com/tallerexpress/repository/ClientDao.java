package com.tallerexpress.repository;
import com.tallerexpress.model.*; import java.sql.*; import java.util.*;
public interface ClientDao { int insert(Client c)throws SQLException; Optional<Client> findById(int id)throws SQLException; List<Client> findAll()throws SQLException; void update(Client c)throws SQLException; void delete(int id)throws SQLException; }
