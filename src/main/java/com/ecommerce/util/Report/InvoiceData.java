package com.ecommerce.util.Report;

public class InvoiceData {
    private String firstName;
    private String lastName;
    private String address;
    private String state;
    private String city;
    private String pincode;
    private String paymentId;
    private String paymentStatus;
    private String orderId;
    private String orderDate;
    private String shippingType;
    private String shippingDeliveryPrice;
    private String totalAmount;

    // GETTERS — MUST match JRXML exactly
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getAddress() { return address; }
    public String getState() { return state; }
    public String getCity() { return city; }
    public String getPincode() { return pincode; }
    public String getPaymentId() { return paymentId; }
    public String getPaymentStatus() { return paymentStatus; }
    public String getOrderId() { return orderId; }
    public String getOrderDate() { return orderDate; }
    public String getShippingType() { return shippingType; }
    public String getShippingDeliveryPrice() { return shippingDeliveryPrice; }
    public String getTotalAmount() { return totalAmount; }

    // SETTERS (optional)
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setAddress(String address) { this.address = address; }
    public void setState(String state) { this.state = state; }
    public void setCity(String city) { this.city = city; }
    public void setPincode(String pincode) { this.pincode = pincode; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public void setOrderDate(String orderDate) { this.orderDate = orderDate; }
    public void setShippingType(String shippingType) { this.shippingType = shippingType; }
    public void setShippingDeliveryPrice(String shippingDeliveryPrice) { this.shippingDeliveryPrice = shippingDeliveryPrice; }
    public void setTotalAmount(String totalAmount) { this.totalAmount = totalAmount; }
}