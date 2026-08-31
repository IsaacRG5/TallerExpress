package com.tallerexpress.service;

import com.tallerexpress.exception.BusinessException;
import com.tallerexpress.model.Client;
import java.util.List;
import java.util.Optional;

public interface ClientService {
    int create(Client client) throws BusinessException;
    void update(Client client) throws BusinessException;
    void delete(int id) throws BusinessException;
    List<Client> list() throws BusinessException;
    Optional<Client> find(int id) throws BusinessException;
}
