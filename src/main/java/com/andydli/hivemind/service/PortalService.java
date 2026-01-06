package com.andydli.hivemind.service;

import com.andydli.hivemind.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.andydli.hivemind.model.Portal;
import com.andydli.hivemind.mapper.PortalMapper;
import com.andydli.hivemind.dto.PortalDTO;
import com.andydli.hivemind.dto.PortalCreationDTO;
import com.andydli.hivemind.dto.PortalEventDTO;
import com.andydli.hivemind.repository.PortalRepository;
import com.andydli.hivemind.model.User;
import com.andydli.hivemind.exceptions.ResourceNotFoundException;
import com.andydli.hivemind.exceptions.ForbiddenOperationException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PortalService {
    private final UserRepository userRepository;
    private final PortalMapper portalMapper;
    private final PortalRepository portalRepository;
    private final WebSocketMessageService webSocketMessageService;

    public PortalService(
            UserRepository userRepository,
            PortalMapper portalMapper,
            PortalRepository portalRepository,
            WebSocketMessageService webSocketMessageService
    ) {
        this.userRepository = userRepository;
        this.portalMapper = portalMapper;
        this.portalRepository = portalRepository;
        this.webSocketMessageService = webSocketMessageService;
    }

    @Transactional(readOnly = true)
    public List<PortalDTO> getAllPortals() {
        List<Portal> portals = portalRepository.findAllWithCreators();
        return portals.stream()
                .map(portalMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public PortalDTO createPortal(PortalCreationDTO portalCreationDTO, Long creatorId) {
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new ResourceNotFoundException("User Not Found"));

        Portal portal = portalMapper.toEntity(portalCreationDTO);
        creator.addPortal(portal);

        Portal savedPortal = portalRepository.save(portal);
        PortalDTO portalDTO = portalMapper.toDTO(savedPortal);

        PortalEventDTO portalEventDTO = new PortalEventDTO("CREATED", portalDTO);
        webSocketMessageService.broadcastMessage(portalEventDTO);

        return portalDTO;
    }

    @Transactional
    public void deletePortal(Long portalId, Long creatorId) {
        Portal portal = portalRepository.findById(portalId)
                .orElseThrow(() -> new ResourceNotFoundException("Portal Not Found"));

        User creator = portal.getCreator();
        if (creator == null || !creator.getId().equals(creatorId)) {
            throw new ForbiddenOperationException("User Not Authorized to Delete this Portal");
        }

        PortalDTO portalDTO = portalMapper.toDTO(portal);
        PortalEventDTO portalEventDTO = new PortalEventDTO("DELETED", portalDTO);
        webSocketMessageService.broadcastMessage(portalEventDTO);

        creator.removePortal(portal);
        portalRepository.delete(portal); // redundant due to orphanRemoval setting, though no harm
    }
}