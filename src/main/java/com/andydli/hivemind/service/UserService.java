package com.andydli.hivemind.service;

import org.springframework.stereotype.Service;
import com.andydli.hivemind.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import com.andydli.hivemind.model.User;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encoder;

    public UserService(UserRepository userRepository, BCryptPasswordEncoder encoder) {
        this.userRepository = userRepository;
        this.encoder = encoder;
    }

    @Transactional
    public User registerUser(User user) {
        // null check
        if (user == null) {
            throw new IllegalArgumentException("User Cannot Be Null");
        }

        // email existence check
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email is Required");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email Already Exists");
        }

        // get plain password during registration
        String plainPassword = user.getPlainPassword();

        // validate plain password
        if (plainPassword == null || plainPassword.isBlank()) {
            throw new IllegalArgumentException("Password Cannot Be Empty");
        }
        if (plainPassword.length() < 8 || plainPassword.length() > 64) {
            throw new IllegalArgumentException("Password Must Be Between 8 and 64 Characters");
        }

        // hash the password
        String hashedPassword = encoder.encode(plainPassword);
        user.setPasswordHash(hashedPassword);

        // clear plain password
        user.setPlainPassword(null);

        return userRepository.save(user);
    }
}