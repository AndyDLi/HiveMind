package com.andydli.hivemind.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.Instant;
import java.util.Set;
import java.util.HashSet;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotBlank(message = "Email is Required")
    @Email(message = "Email Must Be Valid")
    private String email;

    @NotBlank(message = "Password is Required")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) // ensures password is not serialized in responses
    @Getter(AccessLevel.NONE) // prevents getter generation for password
    private String password;
    private static final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @NotBlank(message = "First Name is Required")
    private String firstName;

    @NotBlank(message = "Last Name is Required")
    private String lastName;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    private Set<UserRole> userRoles = new HashSet<>();

    @Column(nullable = false, updatable = false)
    private Instant createdAt;
    @Column(nullable = false)
    private Instant updatedAt;
    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    // Custom Constructor
    public User(String email, String password, String firstName, String lastName) {
        setEmail(email);
        setPassword(password);
        this.firstName = firstName;
        this.lastName = lastName;
        this.userRoles = new HashSet<>();
    }

    // Custom Setters
    public void setEmail(String email) {
        if (email == null) {
            throw new IllegalArgumentException("Email is Required");
        }

        String normalizedEmail = email.trim().toLowerCase();
        if (!normalizedEmail.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new IllegalArgumentException("Email Must Be Valid");
        }

        this.email = normalizedEmail;
    }

    public void setPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Password is Required");
        }

        this.password = passwordEncoder.encode(plainPassword.trim());
    }

    @Override
    public String toString() {
        return String.format("User{id=%d, email='%s', firstName='%s', lastName='%s', createdAt=%s, updatedAt=%s}",
                id, email, firstName, lastName, createdAt, updatedAt);
    }
}