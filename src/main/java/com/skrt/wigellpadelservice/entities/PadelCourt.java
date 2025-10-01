package com.skrt.wigellpadelservice.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "padel_courts", uniqueConstraints = @UniqueConstraint(columnNames = "name"))
public class PadelCourt {

    @Id
    @GeneratedValue
    private UUID id;

    @NotBlank
    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false)
    private int maxPlayers;

    @Column(nullable = false)
    private boolean active = true;

    private LocalDateTime deactivatedAt;

    public PadelCourt() {
    }

    public UUID getId() {
        return id;
    }
    public void setId(UUID id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public int getMaxPlayers() {
        return maxPlayers;
    }
    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }
    public boolean isActive() {
        return active;
    }
    public void setActive(boolean active) {
        this.active = active;
    }
    public LocalDateTime getDeactivatedAt() {
        return deactivatedAt;
    }
    public void setDeactivatedAt(LocalDateTime deactivatedAt) {
        this.deactivatedAt = deactivatedAt;
    }
}
