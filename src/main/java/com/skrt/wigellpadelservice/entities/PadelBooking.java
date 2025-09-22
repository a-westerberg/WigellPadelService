package com.skrt.wigellpadelservice.entities;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(
    name = "padel_bookings",
    indexes = {
        @Index(name = "idx_booking_court_date_time", columnList = "court_id, date, time"),
        @Index(name = "idx_booking_date", columnList = "date")
    }
)
public class PadelBooking {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private PadelCustomer customer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "court_id", nullable = false)
    private PadelCourt court;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private LocalTime time;

    @Column(nullable = false)
    private int players;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalPriceSek;

    @Column(nullable = false)
    private boolean canceled = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    private LocalDateTime canceledAt;

    public PadelBooking() {
    }

    @PrePersist
    void prePersist() {
        if(createdAt == null) createdAt = LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }
    public void setId(UUID id) {
        this.id = id;
    }
    public PadelCustomer getCustomer() {
        return customer;
    }
    public void setCustomer(PadelCustomer customer) {
        this.customer = customer;
    }
    public PadelCourt getCourt() {
        return court;
    }
    public void setCourt(PadelCourt court) {
        this.court = court;
    }
    public LocalDate getDate() {
        return date;
    }
    public void setDate(LocalDate date) {
        this.date = date;
    }
    public LocalTime getTime() {
        return time;
    }
    public void setTime(LocalTime time) {
        this.time = time;
    }
    public int getPlayers() {
        return players;
    }
    public void setPlayers(int players) {
        this.players = players;
    }
    public BigDecimal getTotalPriceSek() {
        return totalPriceSek;
    }
    public void setTotalPriceSek(BigDecimal totalPriceSek) {
        this.totalPriceSek = totalPriceSek;
    }
    public boolean isCanceled() {
        return canceled;
    }
    public void setCanceled(boolean canceled) {
        this.canceled = canceled;
        this.canceledAt = canceled ? LocalDateTime.now() : null;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    public LocalDateTime getCancelledAt() {
        return canceledAt;
    }
    public void setCancelledAt(LocalDateTime cancelledAt) {
        this.canceledAt = cancelledAt;
    }
}
