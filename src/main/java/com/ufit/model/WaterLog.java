package com.ufit.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
public class WaterLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate logDate;
    private double amountMl;        // milliliters
    private LocalDateTime createdAt;

    public WaterLog() {
        this.logDate = LocalDate.now();
        this.createdAt = LocalDateTime.now();
    }

    // --- GETTERS AND SETTERS ---

    public Long getId() { return id; }

    public LocalDate getLogDate() { return logDate; }
    public void setLogDate(LocalDate logDate) { this.logDate = logDate; }

    public double getAmountMl() { return amountMl; }
    public void setAmountMl(double amountMl) { this.amountMl = amountMl; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}
