package com.andydli.hivemind.service;

import org.springframework.stereotype.Service;
import com.andydli.hivemind.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.andydli.hivemind.model.User;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User registerUser(User user) {
        // normalize email and check for uniqueness
        String normalizedEmail = user.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new IllegalArgumentException("Email Already Exists");
        }

        // hash initial plain password
        String plainPassword = user.getPlainPassword();
        String hashedPassword = passwordEncoder.encode(plainPassword);

        // set normalized email, hashed password, and clear plain password
        user.setEmail(normalizedEmail);
        user.setPasswordHash(hashedPassword);
        user.setPlainPassword(null);

        return userRepository.save(user);
    }
}