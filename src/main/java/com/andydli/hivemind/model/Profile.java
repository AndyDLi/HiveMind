package com.andydli.hivemind.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import com.fasterxml.jackson.annotation.JsonBackReference;

import java.util.Set;
import java.util.HashSet;
import java.time.Instant;
import java.util.Objects;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "profiles")
public class Profile {
    @Id
    @Column(name = "user_id")
    private Long id;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @MapsId // shares primary key with User
    @JsonBackReference
    private User user;

    @Column(length = 1500)
    private String bio;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "skills", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "skill", nullable = false)
    private Set<String> skills = new HashSet<>();

    @Column(nullable = false)
    @Min(value = 0, message = "Total Sessions Cannot Be Negative")
    private int totalSessions;

    @Column(nullable = false)
    @DecimalMin(value = "0.0", inclusive = true, message = "Rating Must Be At Least 0.0")
    @DecimalMax(value = "5.0", inclusive = true, message = "Rating Cannot Exceed 5.0")
    private double rating;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
        this.rating = 0.0;
        this.totalSessions = 0;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (! (o instanceof Profile other)) return false;
        return this.id != null && this.id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.id);
    }

    @Override
    public String toString() {
        return String.format("MentorProfile{id=%s, bio='%s', rating=%.2f, totalSessions=%d}",
            id, bio, rating, totalSessions);
    }
}