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
import org.springframework.test.web.servlet.MockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.ObjectMapper;
import com.andydli.hivemind.model.User;
import com.andydli.hivemind.service.ProfileService;
import com.andydli.hivemind.dto.ProfileDTO;
import com.andydli.hivemind.dto.ProfileRequestDTO;
import com.andydli.hivemind.exceptions.ResourceNotFoundException;
import com.andydli.hivemind.exceptions.ProfileAlreadyExistsException;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.Instant;
import java.util.Set;
import java.util.List;

@WebMvcTest(
        controllers = ProfileController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class
        )
)
@Import({TestSecurityConfig.class, GlobalExceptionHandler.class})
public class ProfileControllerTest {
    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProfileService profileService;

    private MockMvc mockMvc;

    private static final Long USER_ID = 1L;
    private static final String PROFILE_BIO = "Profile Bio";
    private static final String UPDATED_PROFILE_BIO = "Updated Profile Bio";
    private static final Set<String> PROFILE_SKILLS = Set.of();
    private static final Set<String> UPDATED_PROFILE_SKILLS = Set.of("Java", "Spring");
    private static final int PROFILE_TOTAL_SESSIONS = 0;
    private static final double PROFILE_RATING = 0.0;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    @DisplayName("GET /api/profiles/me - Authenticated User Returns ProfileDTO")
    void getProfile_whenAuthenticated_returnsProfileDTO() throws Exception {
        User mockUser = new User();
        mockUser.setId(USER_ID);

        ProfileDTO profileDTO = new ProfileDTO(USER_ID, PROFILE_BIO, PROFILE_SKILLS, PROFILE_TOTAL_SESSIONS, PROFILE_RATING, Instant.now(), Instant.now());

        when(profileService.getProfile(USER_ID)).thenReturn(profileDTO);

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(mockUser, null, List.of());

        mockMvc.perform(get("/api/profiles/me")
                .with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(USER_ID))
                .andExpect(jsonPath("$.bio").value(PROFILE_BIO))
                .andExpect(jsonPath("$.skills").isArray())
                .andExpect(jsonPath("$.skills").isEmpty())
                .andExpect(jsonPath("$.totalSessions").value(PROFILE_TOTAL_SESSIONS))
                .andExpect(jsonPath("$.rating").value(PROFILE_RATING))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());
    }

    @Test
    @DisplayName("GET /api/profiles/me - Unauthenticated User Returns 403")
    void getProfile_whenUnauthenticated_returns403() throws Exception {
        mockMvc.perform(get("/api/profiles/me"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/profiles/me - User Not Found Returns 404")
    void getProfile_whenUserNotFound_returns404() throws Exception {
        User mockUser = new User();
        mockUser.setId(USER_ID);

        when(profileService.getProfile(USER_ID)).thenThrow(new ResourceNotFoundException("User Not Found"));

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(mockUser, null, List.of());

        mockMvc.perform(get("/api/profiles/me")
                .with(authentication(auth)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/profiles/me - Profile Not Found Returns 404")
    void getProfile_whenProfileNotFound_returns404() throws Exception {
        User mockUser = new User();
        mockUser.setId(USER_ID);

        when(profileService.getProfile(USER_ID)).thenThrow(new ResourceNotFoundException("Profile Not Found"));

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(mockUser, null, List.of());

        mockMvc.perform(get("/api/profiles/me")
                .with(authentication(auth)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/profiles/me - Valid Request Returns ProfileDTO and 201")
    void createProfile_whenValidRequest_returnsProfileDTO_and201() throws Exception {
        User mockUser = new User();
        mockUser.setId(USER_ID);

        ProfileRequestDTO profileRequestDTO = new ProfileRequestDTO(PROFILE_BIO, PROFILE_SKILLS);
        ProfileDTO expectedProfileDTO = new ProfileDTO(USER_ID, PROFILE_BIO, PROFILE_SKILLS, PROFILE_TOTAL_SESSIONS, PROFILE_RATING, Instant.now(), Instant.now());

        when(profileService.createProfile(USER_ID, profileRequestDTO)).thenReturn(expectedProfileDTO);

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(mockUser, null, List.of());

        mockMvc.perform(post("/api/profiles/me")
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(profileRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(USER_ID))
                .andExpect(jsonPath("$.bio").value(PROFILE_BIO))
                .andExpect(jsonPath("$.skills").isArray())
                .andExpect(jsonPath("$.skills").isEmpty())
                .andExpect(jsonPath("$.totalSessions").value(PROFILE_TOTAL_SESSIONS))
                .andExpect(jsonPath("$.rating").value(PROFILE_RATING))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());
    }

    @Test
    @DisplayName("POST /api/profiles/me - Unauthenticated User Returns 403")
    void createProfile_whenUnauthenticated_returns403() throws Exception {
        ProfileRequestDTO profileRequestDTO = new ProfileRequestDTO(PROFILE_BIO, PROFILE_SKILLS);

        mockMvc.perform(post("/api/profiles/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(profileRequestDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/profiles/me - Missing Fields Returns 400")
    void createProfile_whenMissingFields_returns400() throws Exception {
        User mockUser = new User();
        mockUser.setId(USER_ID);

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(mockUser, null, List.of());
        String emptyJson = "{}";

        mockMvc.perform(post("/api/profiles/me")
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON)
                .content(emptyJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/profiles/me - User Not Found Returns 404")
    void createProfile_whenUserNotFound_returns404() throws Exception {
        User mockUser = new User();
        mockUser.setId(USER_ID);

        ProfileRequestDTO profileRequestDTO = new ProfileRequestDTO(PROFILE_BIO, PROFILE_SKILLS);

        when(profileService.createProfile(USER_ID, profileRequestDTO)).thenThrow(new ResourceNotFoundException("User Not Found"));

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(mockUser, null, List.of());

        mockMvc.perform(post("/api/profiles/me")
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(profileRequestDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/profiles/me - Profile Already Exists Returns 409")
    void createProfile_whenProfileAlreadyExists_returns409() throws Exception {
        User mockUser = new User();
        mockUser.setId(USER_ID);

        ProfileRequestDTO profileRequestDTO = new ProfileRequestDTO(PROFILE_BIO, PROFILE_SKILLS);

        when(profileService.createProfile(USER_ID, profileRequestDTO)).thenThrow(new ProfileAlreadyExistsException("Profile Already Exists for this User"));

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(mockUser, null, List.of());

        mockMvc.perform(post("/api/profiles/me")
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(profileRequestDTO)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("PUT /api/profiles/me - Valid Request Returns Updated ProfileDTO and 200")
    void updateProfile_whenValidRequest_returnsUpdatedProfileDTO_and200() throws Exception {
        User mockUser = new User();
        mockUser.setId(USER_ID);

        ProfileRequestDTO profileRequestDTO = new ProfileRequestDTO(UPDATED_PROFILE_BIO, UPDATED_PROFILE_SKILLS);
        ProfileDTO expectedProfileDTO = new ProfileDTO(USER_ID, UPDATED_PROFILE_BIO, UPDATED_PROFILE_SKILLS, PROFILE_TOTAL_SESSIONS, PROFILE_RATING, Instant.now(), Instant.now());

        when(profileService.updateProfile(USER_ID, profileRequestDTO)).thenReturn(expectedProfileDTO);

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(mockUser, null, List.of());

        mockMvc.perform(put("/api/profiles/me")
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(profileRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(USER_ID))
                .andExpect(jsonPath("$.bio").value(UPDATED_PROFILE_BIO))
                .andExpect(jsonPath("$.skills").isArray())
                .andExpect(jsonPath("$.skills").isNotEmpty())
                .andExpect(jsonPath("$.skills", hasSize(2)))
                .andExpect(jsonPath("$.skills", containsInAnyOrder("Java", "Spring")))
                .andExpect(jsonPath("$.totalSessions").value(PROFILE_TOTAL_SESSIONS))
                .andExpect(jsonPath("$.rating").value(PROFILE_RATING))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());
    }

    @Test
    @DisplayName("PUT /api/profiles/me - Unauthenticated User Returns 403")
    void updateProfile_whenUnauthenticated_returns403() throws Exception {
        ProfileRequestDTO profileRequestDTO = new ProfileRequestDTO(UPDATED_PROFILE_BIO, UPDATED_PROFILE_SKILLS);

        mockMvc.perform(put("/api/profiles/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(profileRequestDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PUT /api/profiles/me - Missing Fields Returns 400")
    void updateProfile_whenMissingFields_returns400() throws Exception {
        User mockUser = new User();
        mockUser.setId(USER_ID);

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(mockUser, null, List.of());
        String emptyJson = "{}";

        mockMvc.perform(put("/api/profiles/me")
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON)
                .content(emptyJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/profiles/me - User Not Found Returns 404")
    void updateProfile_whenUserNotFound_returns404() throws Exception {
        User mockUser = new User();
        mockUser.setId(USER_ID);

        ProfileRequestDTO profileRequestDTO = new ProfileRequestDTO(UPDATED_PROFILE_BIO, UPDATED_PROFILE_SKILLS);

        when(profileService.updateProfile(USER_ID, profileRequestDTO)).thenThrow(new ResourceNotFoundException("User Not Found"));

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(mockUser, null, List.of());

        mockMvc.perform(put("/api/profiles/me")
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(profileRequestDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/profiles/me - Profile Not Found Returns 404")
    void updateProfile_whenProfileNotFound_returns404() throws Exception {
        User mockUser = new User();
        mockUser.setId(USER_ID);

        ProfileRequestDTO profileRequestDTO = new ProfileRequestDTO(UPDATED_PROFILE_BIO, UPDATED_PROFILE_SKILLS);

        when(profileService.updateProfile(USER_ID, profileRequestDTO)).thenThrow(new ResourceNotFoundException("Profile Not Found. Create One First"));

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(mockUser, null, List.of());

        mockMvc.perform(put("/api/profiles/me")
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(profileRequestDTO)))
                .andExpect(status().isNotFound());
    }
}