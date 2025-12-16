package com.andydli.hivemind.service;

import org.springframework.stereotype.Service;
import com.andydli.hivemind.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.andydli.hivemind.mapper.UserMapper;
import com.andydli.hivemind.security.JwtService;
import com.andydli.hivemind.model.User;
import com.andydli.hivemind.dto.UserRegistrationDTO;
import com.andydli.hivemind.dto.UserLoginDTO;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final JwtService jwtService;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            UserMapper userMapper,
            JwtService jwtService)
    {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
        this.jwtService = jwtService;
    }

    @Transactional
    public User registerUser(UserRegistrationDTO userRegistrationDTO) {
        // normalize registration email and check for uniqueness
        String normalizedEmail = userRegistrationDTO.email().trim().toLowerCase();
        if (userRepository.existsByEmail(normalizedEmail)) {
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

    public String authenticateUser(UserLoginDTO userLoginDTO) {
        String normalizedEmail = userLoginDTO.email().trim().toLowerCase();
        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new IllegalArgumentException("Invalid Email or Password"));

        String plainPassword = userLoginDTO.plainPassword();
        if (!user.verifyPassword(passwordEncoder, plainPassword)) {
            throw new IllegalArgumentException("Invalid Email or Password");
        }

        return jwtService.generateToken(user.getId(), user.getEmail());
    }
}