package com.ecommerce.util;

import java.time.LocalDateTime;

public class Color {
    private Colors color;
    private LocalDateTime date; // timestamp when color was applied/selected

    // Constructor assigns current time
    public Color(Colors color) {
        this.color = color;
        this.date = LocalDateTime.now();
    }

    // Constructor with custom time
    public Color(Colors color, LocalDateTime date) {
        this.color = color;
        this.date = date;
    }

    // Getters and Setters
    public Colors getColor() {
        return color;
    }

    public void setColor(Colors color) {
        this.color = color;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }
}
