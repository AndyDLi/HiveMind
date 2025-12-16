package com.andydli.hivemind.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import com.andydli.hivemind.service.UserService;
import com.andydli.hivemind.mapper.UserMapper;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;
import com.andydli.hivemind.model.User;
import com.andydli.hivemind.dto.UserDTO;
import com.andydli.hivemind.dto.UserRegistrationDTO;
import com.andydli.hivemind.dto.UserLoginDTO;
import com.andydli.hivemind.dto.AuthResponseDTO;

@RestController
@RequestMapping("/api/auth")
public class UserController {
    private final UserService userService;
    private final UserMapper userMapper;

    public UserController(UserService userService, UserMapper userMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
    }

    @PostMapping("/register")
    public ResponseEntity<UserDTO> registerUser(@Valid @RequestBody UserRegistrationDTO userRegistrationDTO) {
        User user = userService.registerUser(userRegistrationDTO);
        UserDTO userDTO = userMapper.toDTO(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(userDTO);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> authenticateUser(@Valid @RequestBody UserLoginDTO userLoginDTO) {
        String token = userService.authenticateUser(userLoginDTO);
        return ResponseEntity.ok(new AuthResponseDTO(token, "Bearer"));
    }
}