package com.tallerexpress.service;

import com.tallerexpress.exception.BusinessException;
import com.tallerexpress.model.SparePart;
import java.util.List;

public interface SparePartService {
    int create(SparePart part) throws BusinessException;
    void update(SparePart part) throws BusinessException;
    void toggle(int id) throws BusinessException;
    void delete(int id) throws BusinessException;
    List<SparePart> list(String category, String supplier) throws BusinessException;
}
