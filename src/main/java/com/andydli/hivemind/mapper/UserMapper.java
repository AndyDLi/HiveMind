package com.andydli.hivemind.mapper;

import org.springframework.stereotype.Component;
import com.andydli.hivemind.model.User;
import com.andydli.hivemind.dto.UserDTO;
import com.andydli.hivemind.dto.UserRegistrationDTO;
import com.andydli.hivemind.model.Portal;
import com.andydli.hivemind.dto.PortalDTO;

import java.util.List;
import java.util.ArrayList;

@Component
public class UserMapper {
    private final ProfileMapper profileMapper;

    public UserMapper(ProfileMapper profileMapper) {
        this.profileMapper = profileMapper;
    }

    public UserDTO toDTO(User user) {
        if (user == null) {
            return null;
        }

        return new UserDTO(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                toPortalsDTO(user.getPortals()),
                profileMapper.toDTO(user.getProfile()), // safe to use Mapper since no circular reference
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    private List<PortalDTO> toPortalsDTO(List<Portal> portals) {
        if (portals == null) {
            return new ArrayList<>();
        }

        List<PortalDTO> portalsDTO = new ArrayList<>();
        for (Portal portal : portals) {
            portalsDTO.add(new PortalDTO(
                    portal.getId(),
                    portal.getTopic(),
                    portal.getDescription(),
                    null, // avoid circular reference by not including creator
                    portal.getCreatedAt(),
                    portal.getUpdatedAt()
            ));
        }
        return portalsDTO;
    }

    public User toEntity(UserRegistrationDTO userRegistrationDTO) {
        if (userRegistrationDTO == null) {
            return null;
        }

        User user = new User();
        user.setFirstName(userRegistrationDTO.firstName());
        user.setLastName(userRegistrationDTO.lastName());
        return user;
    }
}