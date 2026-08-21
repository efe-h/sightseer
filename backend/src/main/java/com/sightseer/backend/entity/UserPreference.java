package com.sightseer.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_preferences")
@Getter
@Setter
public class UserPreference {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "fk_user_preferences_user"))
    private User user;

    @Column(nullable = false)
    // has to be between 1 and 5, inclusive
    @Min(1)
    @Max(5)
    private Short history;

    @Column(nullable = false)
    // has to be between 1 and 5, inclusive
    @Min(1)
    @Max(5)
    private Short art;

    @Column(nullable = false)
    // has to be between 1 and 5, inclusive
    @Min(1)
    @Max(5)
    private Short architecture;

    @Column(nullable = false)
    // has to be between 1 and 5, inclusive
    @Min(1)
    @Max(5)
    private Short nature;

    @Column(nullable = false)
    // has to be between 1 and 5, inclusive
    @Min(1)
    @Max(5)
    private Short science;

    @Column(nullable = false)
    // has to be between 1 and 5, inclusive
    @Min(1)
    @Max(5)
    private Short food;

    @Column(nullable = false)
    // has to be between 1 and 5, inclusive
    @Min(1)
    @Max(5)
    private Short entertainment;

    @Column(nullable = false)
    // has to be between 1 and 5, inclusive
    @Min(1)
    @Max(5)
    private Short shopping;

    @Column(nullable = false)
    // has to be between 1 and 5, inclusive
    @Min(1)
    @Max(5)
    private Short views;

    @Column(nullable = false)
    // has to be between 1 and 5, inclusive
    @Min(1)
    @Max(5)
    private Short family;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}