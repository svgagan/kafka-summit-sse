package com.egen.ordereventsproducer.model;

import java.util.Date;

public class Order {

    private String orderId;
    private String storeId;
    private double total;
    private Date createdDate;

    public Order() {
    }

    public Order(String orderId, String storeId, double total, Date createdDate) {
        this.orderId = orderId;
        this.storeId = storeId;
        this.total = total;
        this.createdDate = createdDate;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getStoreId() {
        return storeId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }
}
