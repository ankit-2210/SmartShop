package com.ecommerce.util;

public enum Sizes {
    XS(1, "XS", "Extra Small size"),
    S(2, "S", "Small size"),
    M(3, "M", "Medium size"),
    L(4, "L", "Large size"),
    XL(5, "XL", "Extra Large size"),
    XXL(6, "XXL", "Double Extra Large size");

    private final Integer id;
    private final String name;
    private String description;

    Sizes(Integer id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    // Getters & Setters
    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // Convert DB value → Enum
    public static Sizes fromDbValue(String value) {
        if (value == null) return null;

        // Match enum constant name
        try {
            return Sizes.valueOf(value.toUpperCase().replace(" ", "_"));
        }
        catch (IllegalArgumentException e) {
            // Match by display name
            for (Sizes size : Sizes.values()) {
                if (size.getName().equalsIgnoreCase(value)) {
                    return size;
                }
            }
        }
        return null;
    }
}

