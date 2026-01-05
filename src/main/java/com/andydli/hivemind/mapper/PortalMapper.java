package com.andydli.hivemind.mapper;

import org.springframework.stereotype.Component;
import com.andydli.hivemind.model.Portal;
import com.andydli.hivemind.dto.PortalDTO;
import com.andydli.hivemind.dto.PortalCreationDTO;
import com.andydli.hivemind.model.User;
import com.andydli.hivemind.dto.UserPublicDTO;
import com.andydli.hivemind.exceptions.ResourceNotFoundException;

import java.util.ArrayList;

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

        UserPublicDTO userPublicDTO = new UserPublicDTO(
                creator.getId(),
                creator.getFirstName(),
                creator.getLastName(),
                new ArrayList<>(), // empty list to avoid circular reference (not null for consistency)
                null, // profile omitted to avoid circular reference
                creator.getCreatedAt(),
                creator.getUpdatedAt()
        );

        return new PortalDTO(
                portal.getId(),
                portal.getTopic(),
                portal.getDescription(),
                userPublicDTO,
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