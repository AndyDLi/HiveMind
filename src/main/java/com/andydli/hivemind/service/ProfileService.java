package com.andydli.hivemind.service;

import org.springframework.stereotype.Service;
import com.andydli.hivemind.repository.UserRepository;
import com.andydli.hivemind.mapper.ProfileMapper;
import org.springframework.transaction.annotation.Transactional;
import com.andydli.hivemind.model.Profile;
import com.andydli.hivemind.dto.ProfileDTO;
import com.andydli.hivemind.dto.ProfileRequestDTO;
import com.andydli.hivemind.model.User;
import com.andydli.hivemind.exceptions.ResourceNotFoundException;

@Service
public class ProfileService {
    private final UserRepository userRepository;
    private final ProfileMapper profileMapper;

    public ProfileService(UserRepository userRepository, ProfileMapper profileMapper) {
        this.userRepository = userRepository;
        this.profileMapper = profileMapper;
    }

    @Transactional(readOnly = true)
    public ProfileDTO getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User Not Found"));

        Profile profile = user.getProfile();
        if (profile == null) {
            throw new ResourceNotFoundException("Profile Not Found");
        }

        return profileMapper.toDTO(profile);
    }

    @Transactional
    public ProfileDTO createOrUpdateProfile(Long userId, ProfileRequestDTO profileRequestDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User Not Found"));

        Profile profile = user.getProfile();
        if (profile == null) {
            profile = new Profile();
            user.setProfile(profile);
        }

        profile.setBio(profileRequestDTO.bio());
        profile.setSkills(profileRequestDTO.skills());

        User savedUser = userRepository.save(user);
        return profileMapper.toDTO(savedUser.getProfile());
    }
}