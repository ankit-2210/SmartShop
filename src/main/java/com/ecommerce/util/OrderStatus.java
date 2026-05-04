package com.ecommerce.util;

public enum OrderStatus {

    IN_PROGRESS(1, "In Progress", "Your order has been received and is being processed", "bi-gear"),
    RECEIVED(2, "Received", "Your order has been confirmed by our system", "bi-check2-circle"),
    PACKED(3, "Packed", "Your items have been packed and are ready for shipment", "bi-box-seam"),
    OUT_FOR_DELIVERY(4, "Out for Delivery", "Your package is on its way", "bi-truck"),
    DELIVERED(5, "Delivered", "Your order has been delivered successfully", "bi-house-door"),
    CANCELLED(6, "Cancelled", "Your order has been cancelled", "bi-x-circle"),
    SUCCESS(7, "Success", "Your order was completed successfully", "bi-bag-check");

    private Integer id;
    private String name;
    private String description;
    private final String icon;

    OrderStatus(Integer id, String name, String description, String icon) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.icon = icon;
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIcon() {
        return icon;
    }


    public static OrderStatus fromDbValue(String value) {
        if (value == null) return null;

        // Try matching enum constant (e.g., ORDER_RECEIVED)
        try {
            return OrderStatus.valueOf(value.toUpperCase().replace(" ", "_"));
        } catch (IllegalArgumentException e) {
            // Fallback: match by display name (e.g., "Received")
            for (OrderStatus status : OrderStatus.values()) {
                if (status.getName().equalsIgnoreCase(value)) {
                    return status;
                }
            }
        }

        return null; // or you could throw or return a default like UNKNOWN
    }

}
