package com.tallerexpress.service;
import com.tallerexpress.exception.BusinessException; import com.tallerexpress.model.*; import com.tallerexpress.service.UserCreator; import java.time.LocalDateTime;
/** Decorator: agrega propiedades por defecto sin modificar la lógica base de creación. */
public class DefaultUserPropertiesDecorator implements UserCreator {private final UserCreator wrapped;public DefaultUserPropertiesDecorator(UserCreator wrapped){this.wrapped=wrapped;} public int create(User user,String rawPassword)throws BusinessException{user.setRole(Role.RECEPCIONISTA);user.setStatus(UserStatus.ACTIVO);user.setCreatedAt(LocalDateTime.now());return wrapped.create(user,rawPassword);}}
