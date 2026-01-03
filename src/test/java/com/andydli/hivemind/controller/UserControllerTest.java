package com.andydli.hivemind.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import com.andydli.hivemind.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Import;
import com.andydli.hivemind.config.TestSecurityConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.ObjectMapper;
import org.springframework.test.web.servlet.MockMvc;
import org.junit.jupiter.api.BeforeEach;
import com.andydli.hivemind.service.UserService;
import com.andydli.hivemind.model.User;
import com.andydli.hivemind.dto.UserDTO;
import com.andydli.hivemind.dto.UserRegistrationDTO;
import com.andydli.hivemind.dto.UserLoginDTO;
import com.andydli.hivemind.dto.PortalDTO;
import com.andydli.hivemind.exceptions.GlobalExceptionHandler;
import com.andydli.hivemind.exceptions.PasswordMismatchException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.Instant;
import java.util.List;

@WebMvcTest(
        controllers = UserController.class, // simulates HTTP requests to the UserController
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class
        )
)
@Import({TestSecurityConfig.class, GlobalExceptionHandler.class})
public class UserControllerTest {
        @Autowired
        private WebApplicationContext context;

        @Autowired
        private ObjectMapper objectMapper; // for JSON serialization/deserialization

        @MockitoBean // mock UserService bean and inject into Spring ApplicationContext
        private UserService userService;

        private MockMvc mockMvc;

        private static final Long USER_ID = 1L;
        private static final String USER_EMAIL = "test@example.com";
        private static final String INVALID_USER_EMAIL = "invalid-email";
        private static final String FIRST_NAME = "Test";
        private static final String LAST_NAME = "User";
        private static final List<PortalDTO> USER_PORTALS = List.of();
        private static final String USER_PASSWORD = "12345678";
        private static final String INVALID_PASSWORD = "invalid-password";
        private static final String SHORT_PASSWORD = "short";
        private static final String JWT_TOKEN = "jwt-token";

        @BeforeEach
        void setup() {
                mockMvc = MockMvcBuilders
                        .webAppContextSetup(context)
                        .apply(springSecurity())
                        .build();
        }

        @Test
        @DisplayName("GET /me - Authenticated User Returns UserDTO")
        void getCurrentUser_whenAuthenticated_shouldReturnUserDTO() throws Exception {
                User mockUser = new User(); // @AuthenticationPrincipal does not work with @WithMockUser
                mockUser.setId(USER_ID);
                mockUser.setEmail(USER_EMAIL);
                mockUser.setFirstName(FIRST_NAME);
                mockUser.setLastName(LAST_NAME);

                UserDTO mockUserDTO = new UserDTO(USER_ID, USER_EMAIL, FIRST_NAME, LAST_NAME, USER_PORTALS, null, Instant.now(), Instant.now());
                when(userService.getCurrentUser(USER_ID)).thenReturn(mockUserDTO);

                // create authentication token with mockUser as principal
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(mockUser, null, List.of());

                mockMvc.perform(get("/api/users/me")
                        .with(authentication(auth)))
                        .andExpect(status().isOk())
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.id").value(USER_ID))
                        .andExpect(jsonPath("$.email").value(USER_EMAIL))
                        .andExpect(jsonPath("$.firstName").value(FIRST_NAME))
                        .andExpect(jsonPath("$.lastName").value(LAST_NAME))
                        .andExpect(jsonPath("$.portals").isArray())
                        .andExpect(jsonPath("$.portals").isEmpty())
                        .andExpect(jsonPath("$.profile").doesNotExist())
                        .andExpect(jsonPath("$.createdAt").exists())
                        .andExpect(jsonPath("$.updatedAt").exists());
        }

        @Test
        @DisplayName("GET /me - Unauthenticated User Returns 403")
        void getCurrentUser_whenUnauthenticated_shouldReturn403() throws Exception {
                mockMvc.perform(get("/api/users/me"))
                        .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("POST /register - Valid Registration Returns Created UserDTO and 201")
        void registerUser_whenValidRegistration_shouldReturnCreatedUserDTO_and201() throws Exception {
                UserRegistrationDTO userRegistrationDTO = new UserRegistrationDTO(USER_EMAIL, USER_PASSWORD, USER_PASSWORD, FIRST_NAME, LAST_NAME);
                UserDTO mockUserDTO = new UserDTO(USER_ID, USER_EMAIL, FIRST_NAME, LAST_NAME, USER_PORTALS, null, Instant.now(), Instant.now());

                when(userService.registerUser(any(UserRegistrationDTO.class))).thenReturn(mockUserDTO);

                mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRegistrationDTO)))
                        .andExpect(status().isCreated())
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.id").value(USER_ID))
                        .andExpect(jsonPath("$.email").value(USER_EMAIL))
                        .andExpect(jsonPath("$.firstName").value(FIRST_NAME))
                        .andExpect(jsonPath("$.lastName").value(LAST_NAME))
                        .andExpect(jsonPath("$.portals").isArray())
                        .andExpect(jsonPath("$.portals").isEmpty())
                        .andExpect(jsonPath("$.profile").doesNotExist())
                        .andExpect(jsonPath("$.createdAt").exists())
                        .andExpect(jsonPath("$.updatedAt").exists());
        }

        @Test
        @DisplayName("POST /register - Invalid Email Returns 400")
        void registerUser_whenInvalidEmail_shouldReturn400() throws Exception {
                UserRegistrationDTO userRegistrationDTO = new UserRegistrationDTO(INVALID_USER_EMAIL, USER_PASSWORD, USER_PASSWORD, FIRST_NAME, LAST_NAME);

                mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRegistrationDTO)))
                        .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("POST /register - Password Mismatch Returns 400")
        void registerUser_whenPasswordMismatch_shouldReturn400() throws Exception {
                UserRegistrationDTO userRegistrationDTO = new UserRegistrationDTO(USER_EMAIL, USER_PASSWORD, INVALID_PASSWORD, FIRST_NAME, LAST_NAME);

                when(userService.registerUser(any(UserRegistrationDTO.class)))
                        .thenThrow(new PasswordMismatchException("Passwords Do Not Match"));

                mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRegistrationDTO)))
                        .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("POST /register - Short Password Returns 400")
        void registerUser_whenShortPassword_shouldReturn400() throws Exception {
                UserRegistrationDTO userRegistrationDTO = new UserRegistrationDTO(USER_EMAIL, SHORT_PASSWORD, SHORT_PASSWORD, FIRST_NAME, LAST_NAME);

                mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRegistrationDTO)))
                        .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("POST /register - Missing Required Fields Returns 400")
        void registerUser_whenMissingRequiredFields_shouldReturn400() throws Exception {
                String emptyJson = "{}";

                mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(emptyJson))
                        .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("POST /login - Valid Credentials Returns JWT Cookie and 200")
        void authenticateUser_whenValidCredentials_shouldReturnJWTCookie_and200() throws Exception {
                UserLoginDTO userLoginDTO = new UserLoginDTO(USER_EMAIL, USER_PASSWORD);
                when(userService.authenticateUser(any(UserLoginDTO.class))).thenReturn(JWT_TOKEN);

                mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userLoginDTO)))
                        .andExpect(status().isOk())
                        .andExpect(header().exists("Set-Cookie"))
                        .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("token=")))
                        .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("HttpOnly")))
                        .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("Max-Age=86400")))
                        .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("SameSite=Lax")));
        }

        @Test
        @DisplayName("POST /login - Invalid Email Returns 400")
        void authenticateUser_whenInvalidEmail_shouldReturn400() throws Exception {
                UserLoginDTO userLoginDTO = new UserLoginDTO(INVALID_USER_EMAIL, USER_PASSWORD);

                mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userLoginDTO)))
                        .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("POST /login - Missing Required Fields Returns 400")
        void authenticateUser_whenMissingRequiredFields_shouldReturn400() throws Exception {
                String emptyJson = "{}";

                mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(emptyJson))
                        .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("POST /logout - Should Clear JWT Cookie and Return 200")
        @WithMockUser
        void logout_shouldClearJWTCookie_andReturn200() throws Exception {
                mockMvc.perform(post("/api/users/logout"))
                        .andExpect(status().isOk())
                        .andExpect(header().exists("Set-Cookie"))
                        .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("token=")))
                        .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("Max-Age=0")))
                        .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("HttpOnly")))
                        .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("SameSite=Lax")));
        }
}