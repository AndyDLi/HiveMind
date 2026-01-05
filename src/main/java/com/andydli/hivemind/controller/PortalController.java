package com.andydli.hivemind.controller;

import org.springframework.web.bind.annotation.*;
import com.andydli.hivemind.service.PortalService;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import com.andydli.hivemind.dto.PortalDTO;
import com.andydli.hivemind.dto.PortalCreationDTO;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.andydli.hivemind.model.User;
import org.springframework.http.HttpStatus;

import java.util.List;

@RestController
@RequestMapping("/api/portals")
public class PortalController {
    private final PortalService portalService;

    public PortalController(PortalService portalService) {
        this.portalService = portalService;
    }

    @GetMapping
    public ResponseEntity<List<PortalDTO>> getAllPortals() {
        List<PortalDTO> portals = portalService.getAllPortals();
        return ResponseEntity.ok(portals);
    }

    @PostMapping
    public ResponseEntity<PortalDTO> createPortal(
            @Valid @RequestBody PortalCreationDTO portalCreationDTO,
            @AuthenticationPrincipal User user
    ) {
        PortalDTO portalDTO = portalService.createPortal(portalCreationDTO, user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(portalDTO);
    }

    @DeleteMapping("/{portalId}")
    public ResponseEntity<Void> deletePortal(
            @PathVariable Long portalId,
            @AuthenticationPrincipal User user
    ) {
        portalService.deletePortal(portalId, user.getId());
        return ResponseEntity.noContent().build();
    }
}