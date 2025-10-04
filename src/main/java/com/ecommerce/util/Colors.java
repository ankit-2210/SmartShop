package com.ecommerce.util;

public enum Colors {
    RED(1, "Red", "#e74c3c", "Vibrant red color"),
    GREEN(2, "Green", "#2ecc71", "Nature green color"),
    BLUE(3, "Blue", "#3498db", "Cool blue color"),
    BLACK(4, "Black", "#000000", "Classic black shade"),
    WHITE(5, "White", "#ffffff", "Pure white shade"),
    YELLOW(6, "Yellow", "#f1c40f", "Bright yellow color"),
    ORANGE(7, "Orange", "#e67e22", "Energetic orange color"),
    PURPLE(8, "Purple", "#9b59b6", "Royal purple shade"),
    PINK(9, "Pink", "#fd79a8", "Soft pink color"),
    BROWN(10, "Brown", "#795548", "Warm brown color");

    private final Integer id;
    private final String name;
    private final String hexCode;
    private String description;

    Colors(Integer id, String name, String hexCode, String description) {
        this.id = id;
        this.name = name;
        this.hexCode = hexCode;
        this.description = description;
    }

    // Getters & Setters
    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getHexCode() {
        return hexCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // Convert DB value → Enum
    public static Colors fromDbValue(String value) {
        if (value == null) return null;

        // Match enum constant name
        try {
            return Colors.valueOf(value.toUpperCase().replace(" ", "_"));
        }
        catch (IllegalArgumentException e) {
            // Match by display name
            for (Colors color : Colors.values()) {
                if (color.getName().equalsIgnoreCase(value)) {
                    return color;
                }
            }
        }
        return null;
    }
}
