package com.tallerexpress.model;
import java.math.BigDecimal;
public record OrderPart(int sparePartId,String referenceCode,String name,int quantity,BigDecimal unitPrice) { public BigDecimal total(){return unitPrice.multiply(BigDecimal.valueOf(quantity));} }
