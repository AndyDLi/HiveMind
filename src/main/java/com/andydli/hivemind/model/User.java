package com.andydli.hivemind.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.time.Instant;
import java.util.Set;
import java.util.HashSet;
import java.util.Objects;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Email is Required")
    @Email(message = "Email Must Be Valid")
    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @NotBlank(message = "Password is Required")
    @JsonIgnore // excludes passwordHash from serialization completely
    @Column(nullable = false, name = "password_hash")
    private String passwordHash;

    @NotBlank(message = "First Name is Required")
    @Column(nullable = false, name = "first_name", length = 100)
    private String firstName;

    @NotBlank(message = "Last Name is Required")
    @Column(nullable = false, name = "last_name", length = 100)
    private String lastName;

    private Set<Role> userRoles = new HashSet<>();

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

    // Custom Constructor
    public User(String email, String passwordHash, String firstName, String lastName) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.firstName = firstName;
        this.lastName = lastName;
        this.userRoles = new HashSet<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User other)) return false;
        return email != null && email.equals(other.email);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(email);
    }

    @Override
    public String toString() {
        return String.format("User{id=%d, email='%s', firstName='%s', lastName='%s', createdAt=%s, updatedAt=%s}",
                id, email, firstName, lastName, createdAt, updatedAt);
    }

    private void normalizeEmail() {
        if (this.email != null) {
            this.email = this.email.trim().toLowerCase();
        }
    }
}