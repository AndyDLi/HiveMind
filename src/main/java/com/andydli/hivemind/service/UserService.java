package com.andydli.hivemind.service;

import org.springframework.stereotype.Service;
import com.andydli.hivemind.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.andydli.hivemind.mapper.UserMapper;
import com.andydli.hivemind.model.User;
import com.andydli.hivemind.dto.UserRegistrationDTO;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
    }

    @Transactional
    public User registerUser(UserRegistrationDTO userRegistrationDTO) {
        // normalize registration email and check for uniqueness
        String normalizedEmail = userRegistrationDTO.email().trim().toLowerCase();
        if (userRepository.existsByEmail(userRegistrationDTO.email().trim().toLowerCase())) {
            throw new IllegalArgumentException("Email Already Exists");
        }

        // map DTO to entity
        User user = userMapper.toEntity(userRegistrationDTO);

        // hash initial plain password
        String plainPassword = userRegistrationDTO.plainPassword();
        String hashedPassword = passwordEncoder.encode(plainPassword);

        // set normalized email and hashed password
        user.setEmail(normalizedEmail);
        user.setPasswordHash(hashedPassword);

        return userRepository.save(user);
    }
}