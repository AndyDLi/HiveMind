package com.andydli.hivemind.mapper;

import com.andydli.hivemind.dto.ProfileRequestDTO;
import org.springframework.stereotype.Component;
import com.andydli.hivemind.model.Profile;
import com.andydli.hivemind.dto.ProfileDTO;

import java.util.HashSet;

@Component
public class ProfileMapper {
    public ProfileDTO toDTO(Profile profile) {
        if (profile == null) {
            return null;
        }

        return new ProfileDTO(
                profile.getId(),
                profile.getBio(),
                profile.getSkills() != null ? profile.getSkills() : new HashSet<>(),
                profile.getTotalSessions(),
                profile.getRating(),
                profile.getCreatedAt(),
                profile.getUpdatedAt()
        );
    }

    public Profile toEntity(ProfileRequestDTO profileRequestDTO) {
        if (profileRequestDTO == null) {
            return null;
        }

        Profile profile = new Profile();
        profile.setBio(profileRequestDTO.bio());
        profile.setSkills(profileRequestDTO.skills());
        return profile;
    }
}