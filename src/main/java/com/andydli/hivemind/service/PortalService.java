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
import com.andydli.hivemind.exceptions.ForbiddenOperationException;

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
        creator.addPortal(portal);

        Portal savedPortal = portalRepository.save(portal);
        return portalMapper.toDTO(savedPortal);
    }

    @Transactional
    public void deletePortal(Long portalId, Long creatorId) {
        Portal portal = portalRepository.findById(portalId)
                .orElseThrow(() -> new ResourceNotFoundException("Portal Not Found"));

        User creator = portal.getCreator();
        if (creator == null || !creator.getId().equals(creatorId)) {
            throw new ForbiddenOperationException("User Not Authorized to Delete this Portal");
        }

        creator.removePortal(portal); // redundant due to cascade setting, though no harm

        portalRepository.delete(portal);
    }
}