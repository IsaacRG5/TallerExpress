package com.tallerexpress.service;
import com.tallerexpress.exception.BusinessException; import com.tallerexpress.model.User;
public interface UserCreator { int create(User user,String rawPassword)throws BusinessException; }
