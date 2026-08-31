package com.tallerexpress.service;
import com.tallerexpress.exception.*; import com.tallerexpress.model.*; import java.util.*;
public interface UserService extends UserCreator { Optional<User> login(String username,String password)throws BusinessException; List<User> list()throws BusinessException; void update(User user)throws BusinessException; void delete(int id)throws BusinessException; }
