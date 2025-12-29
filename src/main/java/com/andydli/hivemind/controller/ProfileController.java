package com.andydli.hivemind.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import com.andydli.hivemind.service.ProfileService;
import com.andydli.hivemind.model.User;
import com.andydli.hivemind.dto.ProfileDTO;
import com.andydli.hivemind.dto.ProfileRequestDTO;

@RestController
@RequestMapping("/api/profiles")
public class ProfileController {
    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping("/me")
    public ResponseEntity<ProfileDTO> getProfile(@AuthenticationPrincipal User user) {
        ProfileDTO profileDTO = profileService.getProfile(user.getId());
        return ResponseEntity.ok(profileDTO);
    }

    @PostMapping("/me")
    public ResponseEntity<ProfileDTO> createProfile(
            @Valid @RequestBody ProfileRequestDTO profileRequestDTO,
            @AuthenticationPrincipal User user
    ) {
        ProfileDTO profileDTO = profileService.createOrUpdateProfile(user.getId(), profileRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(profileDTO);
    }

    @PutMapping("/me")
    public ResponseEntity<ProfileDTO> updateProfile(
            @Valid @RequestBody ProfileRequestDTO profileRequestDTO,
            @AuthenticationPrincipal User user
    ) {
        ProfileDTO profileDTO = profileService.createOrUpdateProfile(user.getId(), profileRequestDTO);
        return ResponseEntity.ok(profileDTO);
    }
}