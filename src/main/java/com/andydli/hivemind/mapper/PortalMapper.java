package com.andydli.hivemind.mapper;

import org.springframework.stereotype.Component;
import com.andydli.hivemind.model.Portal;
import com.andydli.hivemind.dto.PortalDTO;
import com.andydli.hivemind.dto.PortalCreationDTO;
import com.andydli.hivemind.model.User;
import com.andydli.hivemind.dto.UserDTO;
import com.andydli.hivemind.exceptions.ResourceNotFoundException;

@Component
public class PortalMapper {
    public PortalDTO toDTO(Portal portal) {
        if (portal == null) {
            return null;
        }

        User creator = portal.getCreator();
        if (creator == null) {
            throw new ResourceNotFoundException("Portal Creator Not Found");
        }

        UserDTO userDTO = new UserDTO(
                creator.getId(),
                creator.getEmail(),
                creator.getFirstName(),
                creator.getLastName(),
                creator.getCreatedAt(),
                creator.getUpdatedAt()
        );

        return new PortalDTO(
                portal.getId(),
                portal.getTopic(),
                portal.getDescription(),
                userDTO,
                portal.getCreatedAt(),
                portal.getUpdatedAt()
        );
    }

    public Portal toEntity(PortalCreationDTO portalCreationDTO) {
        if (portalCreationDTO == null) {
            return null;
        }

        Portal portal = new Portal();
        portal.setTopic(portalCreationDTO.topic());
        portal.setDescription(portalCreationDTO.description());
        return portal;
    }
}