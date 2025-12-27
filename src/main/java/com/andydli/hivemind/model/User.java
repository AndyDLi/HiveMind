package com.andydli.hivemind.model;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Objects;
import java.util.List;
import java.util.ArrayList;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) // prevents serialization, allows deserialization
    @Getter(AccessLevel.NONE) // do not generate public getter
    @Column(nullable = false, name = "password_hash")
    private String passwordHash;

    @Column(nullable = false, name = "first_name", length = 100)
    private String firstName;

    @Column(nullable = false, name = "last_name", length = 100)
    private String lastName;

    @OneToMany(mappedBy = "creator", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY) // non-owning side
    @JsonManagedReference
    private List<Portal> portals = new ArrayList<>();

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private Profile profile;

    @Column(nullable = false, updatable = false, name = "created_at")
    private Instant createdAt;

    @Column(nullable = false, name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
        normalizeEmail();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
        normalizeEmail();
    }

    private void normalizeEmail() {
        if (this.email != null) {
            this.email = this.email.trim().toLowerCase();
        }
    }

    public boolean verifyPassword(PasswordEncoder passwordEncoder, String plainPassword) {
        return passwordEncoder.matches(plainPassword, this.passwordHash);
    }

    public void addPortal(Portal portal) {
        if (portal == null) return;
        if (this.portals.contains(portal)) return;

        this.portals.add(portal);
        portal.setCreator(this);
    }

    public void removePortal(Portal portal) {
        if (portal == null) return;
        if (!this.portals.contains(portal)) return;

        this.portals.remove(portal);
        portal.setCreator(null);
    }

    public void setProfile(Profile profile) {
        if (profile == null) {
            if (this.profile != null) {
                this.profile.setUser(null);
            }
        } else {
            profile.setUser(this);
        }
        this.profile = profile;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User other)) return false;
        return this.id != null && this.id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.id);
    }

    @Override
    public String toString() {
        return String.format("User{id=%s, email='%s', firstName='%s', lastName='%s', createdAt=%s, updatedAt=%s}",
                id, email, firstName, lastName, createdAt, updatedAt);
    }
}