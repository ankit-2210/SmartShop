package com.ecommerce.util;

import java.time.LocalDateTime;

public class OrderStep {
    private OrderStatus status;
    private LocalDateTime date; // timestamp when status is reached

    public OrderStep(OrderStatus status) {
        this.status = status;
        this.date = LocalDateTime.now(); // assign current time automatically
    }

    public OrderStep(OrderStatus status, LocalDateTime date) {
        this.status = status;
        this.date = date;
    }

    public OrderStatus getStatus() {
        return status;
    }
    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public LocalDateTime getDate() {
        return date;
    }
    public void setDate(LocalDateTime date) {
        this.date = date;
    }
}