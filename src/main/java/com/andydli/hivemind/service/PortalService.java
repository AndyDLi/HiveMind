package com.andydli.hivemind.service;

import com.andydli.hivemind.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.andydli.hivemind.model.Portal;
import com.andydli.hivemind.mapper.PortalMapper;
import com.andydli.hivemind.dto.PortalDTO;
import com.andydli.hivemind.dto.PortalCreationDTO;
import com.andydli.hivemind.repository.PortalRepository;
import com.andydli.hivemind.model.User;
import com.andydli.hivemind.exceptions.ResourceNotFoundException;

@Service
public class PortalService {
    private final UserRepository userRepository;
    private final PortalMapper portalMapper;
    private final PortalRepository portalRepository;

    public PortalService(UserRepository userRepository, PortalMapper portalMapper, PortalRepository portalRepository) {
        this.userRepository = userRepository;
        this.portalMapper = portalMapper;
        this.portalRepository = portalRepository;
    }

    @Transactional
    public PortalDTO createPortal(PortalCreationDTO portalCreationDTO, Long creatorId) {
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new ResourceNotFoundException("User Not Found"));

        Portal portal = portalMapper.toEntity(portalCreationDTO);
        portal.setCreator(creator);

        Portal savedPortal = portalRepository.save(portal);
        return portalMapper.toDTO(savedPortal);
    }
}