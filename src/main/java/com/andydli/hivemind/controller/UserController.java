package com.andydli.hivemind.controller;

import org.springframework.web.bind.annotation.*;
import com.andydli.hivemind.service.UserService;
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
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser(@AuthenticationPrincipal User user) {
        UserDTO userDTO = userService.getCurrentUser(user.getId());
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
                .httpOnly(true) // prevents cookie hijacking via XSS
                .secure(false) // set to true in prod to only send cookies over HTTPS
                .path("/")
                .maxAge(24 * 60 * 60)
                .sameSite("Lax") // don't send cookie on cross-site requests; change to "Strict" in prod
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