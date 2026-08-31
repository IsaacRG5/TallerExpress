package com.tallerexpress.service;
import com.tallerexpress.exception.*; import com.tallerexpress.model.*; import java.math.*; import java.util.*;
public interface ServiceOrderService { int create(ServiceOrder order)throws BusinessException; void updateStatus(int id,OrderStatus status,BigDecimal cost,String diagnosis)throws BusinessException; Optional<ServiceOrder> find(int id)throws BusinessException; List<ServiceOrder> historyByVehicle(int vehicleId)throws BusinessException; BigDecimal calculateTotal(ServiceOrder order); }
