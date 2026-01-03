package com.andydli.hivemind.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import com.andydli.hivemind.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Import;
import com.andydli.hivemind.config.TestSecurityConfig;
import com.andydli.hivemind.exceptions.GlobalExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import com.andydli.hivemind.model.User;
import com.andydli.hivemind.dto.UserDTO;
import com.andydli.hivemind.service.PortalService;
import com.andydli.hivemind.dto.PortalDTO;
import com.andydli.hivemind.dto.PortalCreationDTO;
import com.andydli.hivemind.exceptions.ResourceNotFoundException;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.Instant;
import java.util.List;

@WebMvcTest(
        controllers = PortalController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class
        )
)
@Import({TestSecurityConfig.class, GlobalExceptionHandler.class})
public class PortalControllerTest {
    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PortalService portalService;

    private MockMvc mockMvc;

    private static final Long USER_ID = 1L;
    private static final String USER_EMAIL = "test@example.com";
    private static final String USER_FIRST_NAME = "Test";
    private static final String USER_LAST_NAME = "User";
    private static final List<PortalDTO> USER_PORTALS = List.of();

    private static final Long PORTAL_ID = 1L;
    private static final Long INVALID_PORTAL_ID = 2L;
    private static final String INVALID_PORTAL_ID_TYPE = "invalid-id";
    private static final String PORTAL_TOPIC = "Test Portal Topic";
    private static final String PORTAL_DESCRIPTION = "Test Portal Description";

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    @DisplayName("POST /api/portals - Valid Portal Creation Returns PortalDTO and 201")
    void createPortal_whenValidRequest_shouldReturnPortalDTO_and201() throws Exception {
        User mockUser = new User();
        mockUser.setId(USER_ID);
        mockUser.setEmail(USER_EMAIL);
        mockUser.setFirstName(USER_FIRST_NAME);
        mockUser.setLastName(USER_LAST_NAME);

        PortalCreationDTO portalCreationDTO = new PortalCreationDTO(PORTAL_TOPIC, PORTAL_DESCRIPTION);
        UserDTO userDTO = new UserDTO(USER_ID, USER_EMAIL, USER_FIRST_NAME, USER_LAST_NAME, USER_PORTALS, null, Instant.now(), Instant.now());
        PortalDTO expectedPortalDTO = new PortalDTO(PORTAL_ID, PORTAL_TOPIC, PORTAL_DESCRIPTION, userDTO, Instant.now(), Instant.now());

        when(portalService.createPortal(portalCreationDTO, USER_ID)).thenReturn(expectedPortalDTO);

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(mockUser, null, List.of());

        mockMvc.perform(post("/api/portals")
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(portalCreationDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(PORTAL_ID))
                .andExpect(jsonPath("$.topic").value(PORTAL_TOPIC))
                .andExpect(jsonPath("$.description").value(PORTAL_DESCRIPTION))
                .andExpect(jsonPath("$.creator.id").value(USER_ID))
                .andExpect(jsonPath("$.creator.email").value(USER_EMAIL))
                .andExpect(jsonPath("$.creator.firstName").value(USER_FIRST_NAME))
                .andExpect(jsonPath("$.creator.lastName").value(USER_LAST_NAME))
                .andExpect(jsonPath("$.creator.portals").isArray())
                .andExpect(jsonPath("$.creator.portals").isEmpty())
                .andExpect(jsonPath("$.creator.profile").doesNotExist())
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());
    }

    @Test
    @DisplayName("POST /api/portals - Unauthenticated User Returns 403")
    void createPortal_whenUnauthenticated_shouldReturn403() throws Exception {
        PortalCreationDTO portalCreationDTO = new PortalCreationDTO(PORTAL_TOPIC, PORTAL_DESCRIPTION);

        mockMvc.perform(post("/api/portals")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(portalCreationDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/portals - Missing Fields Returns 400")
    void createPortal_whenMissingFields_shouldReturn400() throws Exception {
        User mockUser = new User();
        mockUser.setId(USER_ID);

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(mockUser, null, List.of());
        String emptyJson = "{}";

        mockMvc.perform(post("/api/portals")
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON)
                .content(emptyJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("DELETE /api/portals/{portalId} - Valid Deletion Returns 204")
    void deletePortal_whenValidRequest_shouldReturn204() throws Exception {
        User mockUser = new User();
        mockUser.setId(USER_ID);

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(mockUser, null, List.of());

        mockMvc.perform(delete("/api/portals/{portalId}", PORTAL_ID)
                .with(authentication(auth)))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/portals/{portalId} - Unauthenticated User Returns 403")
    void deletePortal_whenUnauthenticated_shouldReturn403() throws Exception {
        mockMvc.perform(delete("/api/portals/{portalId}", PORTAL_ID))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE /api/portals/{portalId} - Non-Existent Portal Returns 404")
    void deletePortal_whenNonExistentPortal_shouldReturn404() throws Exception {
        User mockUser = new User();
        mockUser.setId(USER_ID);

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(mockUser, null, List.of());

        doThrow(new ResourceNotFoundException("Portal Not Found"))
                .when(portalService).deletePortal(INVALID_PORTAL_ID, USER_ID);

        mockMvc.perform(delete("/api/portals/{portalId}", INVALID_PORTAL_ID)
                .with(authentication(auth)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/portals/{portalId} - Invalid PortalID Returns 400")
    void deletePortal_whenInvalidPortalId_shouldReturn400() throws Exception {
        User mockUser = new User();
        mockUser.setId(USER_ID);

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(mockUser, null, List.of());

        mockMvc.perform(delete("/api/portals/{portalId}", INVALID_PORTAL_ID_TYPE)
                .with(authentication(auth)))
                .andExpect(status().isBadRequest());
    }
}