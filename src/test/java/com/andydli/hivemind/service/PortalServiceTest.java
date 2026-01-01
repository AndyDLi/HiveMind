package com.andydli.hivemind.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import com.andydli.hivemind.repository.UserRepository;
import com.andydli.hivemind.repository.PortalRepository;
import com.andydli.hivemind.model.Portal;
import com.andydli.hivemind.dto.PortalDTO;
import com.andydli.hivemind.dto.PortalCreationDTO;
import com.andydli.hivemind.mapper.PortalMapper;
import com.andydli.hivemind.model.User;
import com.andydli.hivemind.dto.UserDTO;
import com.andydli.hivemind.exceptions.ResourceNotFoundException;
import com.andydli.hivemind.exceptions.ForbiddenOperationException;

import java.util.ArrayList;
import java.util.Optional;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PortalServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private PortalMapper portalMapper;

    @Mock
    private PortalRepository portalRepository;

    @InjectMocks
    private PortalService portalService;

    @Mock
    private User user;

    @Mock
    private Portal portal;

    private static final Long USER_ID = 1L;
    private static final Long UNAUTHORIZED_USER_ID = 2L;
    private static final String USER_EMAIL = "test@example.com";
    private static final String USER_FIRST_NAME = "Test";
    private static final String USER_LAST_NAME = "User";

    private static final Long PORTAL_ID = 100L;
    private static final String PORTAL_TOPIC = "Test Portal";
    private static final String PORTAL_DESCRIPTION = "Test Description";

    @Test
    @DisplayName("Creating Portal When User Exists Should Save Portal and Establish Bi-Directional Relationship")
    void createPortal_whenUserExists_shouldSavePortalAndEstablishBiDirectionalRelationship() {
        PortalCreationDTO portalCreationDTO = new PortalCreationDTO(PORTAL_TOPIC, PORTAL_DESCRIPTION);

        Portal newPortal = new Portal();
        Portal savedPortal = new Portal();
        savedPortal.setId(PORTAL_ID);

        UserDTO userDTO = new UserDTO(USER_ID, USER_EMAIL, USER_FIRST_NAME, USER_LAST_NAME, new ArrayList<>(), null, Instant.now(), Instant.now());
        PortalDTO expectedPortalDTO = new PortalDTO(PORTAL_ID, PORTAL_TOPIC, PORTAL_DESCRIPTION, userDTO, Instant.now(), Instant.now());

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(portalMapper.toEntity(portalCreationDTO)).thenReturn(newPortal);
        when(portalRepository.save(newPortal)).thenReturn(savedPortal);
        when(portalMapper.toDTO(savedPortal)).thenReturn(expectedPortalDTO);

        PortalDTO result = portalService.createPortal(portalCreationDTO, USER_ID);

        assertNotNull(result, "Created Portal Should Not Be Null");
        assertEquals(expectedPortalDTO, result, "Created Portal Should Match Expected");
        assertEquals(PORTAL_ID, result.id(), "Created Portal Should Have Set ID");
        assertEquals(PORTAL_TOPIC, result.topic(), "Portal Topic Should Match");
        assertEquals(PORTAL_DESCRIPTION, result.description(), "Portal Description Should Match");
        assertEquals(userDTO, result.creator(), "Portal Creator Should Match");

        ArgumentCaptor<Portal> captor = ArgumentCaptor.forClass(Portal.class);
        verify(userRepository).findById(USER_ID);
        verify(portalMapper).toEntity(portalCreationDTO);
        verify(user).addPortal(newPortal);
        verify(portalRepository).save(captor.capture());
        verify(portalMapper).toDTO(savedPortal);

        Portal capturedPortal = captor.getValue();
        assertEquals(newPortal, capturedPortal, "Persisted Portal Should Match Created Portal");
    }

    @Test
    @DisplayName("Creating Portal When User Does Not Exist Should Throw Exception")
    void createPortal_whenUserDoesNotExist_shouldThrowException() {
        PortalCreationDTO portalCreationDTO = new PortalCreationDTO(PORTAL_TOPIC, PORTAL_DESCRIPTION);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> {
           portalService.createPortal(portalCreationDTO, USER_ID);
        });

        assertEquals("User Not Found", ex.getMessage());
        verify(userRepository).findById(USER_ID);
        verify(portalMapper, never()).toEntity(any());
        verify(portalRepository, never()).save(any());
        verify(portalMapper, never()).toDTO(any());
    }

    @Test
    @DisplayName("Deleting Valid Portal Should Delete Portal and Remove Bi-Directional Relationship")
    void deletePortal_whenValidPortal_shouldDeletePortalAndRemoveBiDirectionalRelationship() {
        when(portalRepository.findById(PORTAL_ID)).thenReturn(Optional.of(portal));
        when(portal.getCreator()).thenReturn(user);
        when(user.getId()).thenReturn(USER_ID);

        portalService.deletePortal(PORTAL_ID, USER_ID);

        ArgumentCaptor<Portal> captor = ArgumentCaptor.forClass(Portal.class);
        verify(portalRepository).findById(PORTAL_ID);
        verify(portal).getCreator();
        verify(user).getId();
        verify(user).removePortal(captor.capture());
        verify(portalRepository).delete(portal);

        Portal removedPortal = captor.getValue();
        assertEquals(portal, removedPortal, "Removed Portal Should Match Deleted Portal");
    }

    @Test
    @DisplayName("Deleting Portal When Portal Does Not Exist Should Throw Exception")
    void deletePortal_whenPortalDoesNotExist_shouldThrowException() {
        when(portalRepository.findById(PORTAL_ID)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> {
              portalService.deletePortal(PORTAL_ID, USER_ID);
        });

        assertEquals("Portal Not Found", ex.getMessage());
        verify(portalRepository).findById(PORTAL_ID);
        verify(portalRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Deleting Portal When User Is Null Should Throw Exception")
    void deletePortal_whenUserIsNull_shouldThrowException() {
        when(portalRepository.findById(PORTAL_ID)).thenReturn(Optional.of(portal));
        when(portal.getCreator()).thenReturn(null);

        ForbiddenOperationException ex = assertThrows(ForbiddenOperationException.class, () -> {
              portalService.deletePortal(PORTAL_ID, USER_ID);
        });

        assertEquals("User Not Authorized to Delete this Portal", ex.getMessage());
        verify(portalRepository).findById(PORTAL_ID);
        verify(portal).getCreator();
        verify(portalRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Deleting Portal When User Is Not Creator Should Throw Exception")
    void deletePortal_whenUserIsNotCreator_shouldThrowException() {
        when(portalRepository.findById(PORTAL_ID)).thenReturn(Optional.of(portal));
        when(portal.getCreator()).thenReturn(user);
        when(user.getId()).thenReturn(USER_ID);

        ForbiddenOperationException ex = assertThrows(ForbiddenOperationException.class, () -> {
              portalService.deletePortal(PORTAL_ID, UNAUTHORIZED_USER_ID);
        });

        assertEquals("User Not Authorized to Delete this Portal", ex.getMessage());
        verify(portalRepository).findById(PORTAL_ID);
        verify(portal).getCreator();
        verify(user).getId();
        verify(user, never()).removePortal(any());
        verify(portalRepository, never()).delete(any());
    }
}