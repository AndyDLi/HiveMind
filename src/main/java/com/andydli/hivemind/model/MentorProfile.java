package com.andydli.hivemind.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.HashSet;
import java.time.Instant;
import java.util.Objects;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "mentor_profiles")
public class MentorProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(length = 1500)
    private String bio;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "mentor_skills", joinColumns = @JoinColumn(name = "mentor_profile_id"))
    @Column(name = "skill", nullable = false)
    private Set<String> skills = new HashSet<>();

    @Column(nullable = false)
    private int totalSessions;

    @Column(nullable = false)
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

    // Custom Constructor
    public MentorProfile(User user, String bio, Set<String> skills) {
        this.user = user;
        this.bio = bio;
        this.skills = skills != null ? skills : new HashSet<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (! (o instanceof MentorProfile other)) return false;
        return this.id != null && this.id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.id);
    }

    @Override
    public String toString() {
        return String.format("MentorProfile{id=%s, bio='%s', skills=%s, rating=%.2f}",
            id, bio, skills, rating);
    }
}