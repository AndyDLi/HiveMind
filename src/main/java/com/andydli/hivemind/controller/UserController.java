package com.andydli.hivemind.controller;

import org.springframework.web.bind.annotation.*;
import com.andydli.hivemind.service.UserService;
import com.andydli.hivemind.mapper.UserMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import com.andydli.hivemind.model.User;
import com.andydli.hivemind.dto.UserDTO;
import com.andydli.hivemind.dto.UserRegistrationDTO;
import com.andydli.hivemind.dto.UserLoginDTO;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseCookie;
import org.springframework.http.HttpHeaders;

@RestController
@RequestMapping("/api/user")
public class UserController {
    private final UserService userService;
    private final UserMapper userMapper;

    public UserController(UserService userService, UserMapper userMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
    }

    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser(@AuthenticationPrincipal User user) {
        UserDTO userDTO = userMapper.toDTO(user);
        return ResponseEntity.ok(userDTO);
    }

    @PostMapping("/register")
    public ResponseEntity<UserDTO> registerUser(@Valid @RequestBody UserRegistrationDTO userRegistrationDTO) {
        UserDTO userDTO = userService.registerUser(userRegistrationDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(userDTO);
    }

    @PostMapping("/login")
    public ResponseEntity<Void> authenticateUser(
            @Valid @RequestBody UserLoginDTO userLoginDTO,
            HttpServletResponse response
    ) {
        String token = userService.authenticateUser(userLoginDTO);
        // store token in httpOnly cookie
        ResponseCookie jwtCookie = ResponseCookie.from("token", token)
                .httpOnly(true)
                .secure(false) // set to true in production with HTTPS
                .path("/")
                .maxAge(24 * 60 * 60)
                .sameSite("Lax") // "Lax" for localhost, "Strict" for production
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, jwtCookie.toString());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        ResponseCookie deleteCookie = ResponseCookie.from("token", "")
                .httpOnly(true)
                .secure(false) // match login cookie settings
                .path("/")
                .maxAge(0) // delete cookie
                .sameSite("Lax") // match login cookie settings
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, deleteCookie.toString());
        return ResponseEntity.ok().build();
    }
}