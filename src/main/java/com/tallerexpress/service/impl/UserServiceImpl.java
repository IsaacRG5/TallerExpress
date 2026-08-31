package com.tallerexpress.service.impl;

import com.tallerexpress.repository.UserDao;
import com.tallerexpress.exception.AuthenticationException;
import com.tallerexpress.exception.BusinessException;
import com.tallerexpress.exception.DuplicateException;
import com.tallerexpress.model.Role;
import com.tallerexpress.model.User;
import com.tallerexpress.model.UserStatus;
import com.tallerexpress.service.UserService;
import com.tallerexpress.config.Db;
import com.tallerexpress.config.HttpLog;
import com.tallerexpress.config.PasswordUtil;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class UserServiceImpl implements UserService {
    private final UserDao dao;
    public UserServiceImpl(UserDao dao) { this.dao = dao; }

    @Override public int create(User user, String rawPassword) throws BusinessException {
        validateCreate(user, rawPassword);
        try {
            if (dao.findByUsername(user.getUsername()).isPresent()) throw new DuplicateException("El usuario ya existe.");
            user.setPasswordHash(PasswordUtil.sha256(rawPassword));
            if (user.getCreatedAt() == null) user.setCreatedAt(LocalDateTime.now());
            if (user.getRole() == null) user.setRole(Role.RECEPCIONISTA);
            if (user.getStatus() == null) user.setStatus(UserStatus.ACTIVO);
            try (Connection c = Db.getConnection()) {
                int id = dao.insert(user, c);
                HttpLog.log("POST", "/usuarios", user.getUsername());
                return id;
            }
        } catch (BusinessException e) { throw e; }
        catch (SQLException e) { throw new BusinessException("No se pudo crear el usuario. Verifique que el nombre no esté duplicado.", e); }
    }

    @Override public Optional<User> login(String username, String password) throws BusinessException {
        if (username == null || username.isBlank() || password == null || password.isBlank()) throw new AuthenticationException("Usuario y contraseña son obligatorios.");
        try {
            Optional<User> found = dao.findByUsername(username.trim());
            if (found.isEmpty() || found.get().getStatus() != UserStatus.ACTIVO || !found.get().getPasswordHash().equals(PasswordUtil.sha256(password))) {
                throw new AuthenticationException("Credenciales inválidas o usuario inactivo.");
            }
            HttpLog.log("POST", "/login", username.trim());
            return found;
        } catch (AuthenticationException e) { throw e; }
        catch (SQLException e) { throw new BusinessException("Error de autenticación.", e); }
    }

    @Override public List<User> list() throws BusinessException {
        try { HttpLog.log("GET", "/usuarios", ""); return dao.findAll(); }
        catch (SQLException e) { throw new BusinessException("No se pudieron listar usuarios.", e); }
    }

    @Override public void update(User user) throws BusinessException {
        if (user == null || user.getId() <= 0 || user.getFullName() == null || user.getFullName().isBlank() || user.getRole() == null || user.getStatus() == null) throw new BusinessException("Los datos del usuario no son válidos.");
        try { dao.update(user); HttpLog.log("PATCH", "/usuarios/" + user.getId(), user.getUsername()); }
        catch (SQLException e) { throw new BusinessException("No se pudo actualizar usuario.", e); }
    }

    @Override public void delete(int id) throws BusinessException {
        if (id <= 0) throw new BusinessException("El ID del usuario no es válido.");
        try { dao.delete(id); HttpLog.log("DELETE", "/usuarios/" + id, ""); }
        catch (SQLException e) { throw new BusinessException("No se pudo eliminar usuario.", e); }
    }

    private void validateCreate(User user, String password) throws BusinessException {
        if (user == null) throw new BusinessException("El usuario es obligatorio.");
        if (user.getUsername() == null || user.getUsername().isBlank()) throw new BusinessException("El nombre de usuario es obligatorio.");
        if (user.getFullName() == null || user.getFullName().isBlank()) throw new BusinessException("El nombre completo es obligatorio.");
        if (password == null || password.isBlank()) throw new BusinessException("La contraseña es obligatoria.");
    }
}
