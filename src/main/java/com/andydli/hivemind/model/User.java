package com.andydli.hivemind.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;

import java.time.Instant;
import java.util.Objects;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Email is Required")
    @Email(message = "Email Must Be Valid")
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank(message = "Password Hash is Required")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) // prevents serialization, allows deserialization
    @Getter(AccessLevel.NONE) // do not generate public getter
    @Column(nullable = false, name = "password_hash")
    private String passwordHash;

    @Transient
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String plainPassword; // used only for receiving plain password during registration

    @NotBlank(message = "First Name is Required")
    @Size(min = 1, max = 100, message = "First Name Must Be Between 1 and 100 Characters")
    @Column(nullable = false, name = "first_name", length = 100)
    private String firstName;

    @NotBlank(message = "Last Name is Required")
    @Size(min = 1, max = 100, message = "Last Name Must Be Between 1 and 100 Characters")
    @Column(nullable = false, name = "last_name", length = 100)
    private String lastName;

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
    }

    // Package-Private Getter
    String getPasswordHash() {
        return this.passwordHash;
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

    private void normalizeEmail() {
        if (this.email != null) {
            this.email = this.email.trim().toLowerCase();
        }
    }
}