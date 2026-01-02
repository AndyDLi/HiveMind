package com.andydli.hivemind.controller;

import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import com.andydli.hivemind.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Import;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.test.web.servlet.MockMvc;
import com.andydli.hivemind.config.TestSecurityConfig;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = ViewController.class, // simulates HTTP requests to the ViewController
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class // exclude security filter for testing
        )
)
@Import(TestSecurityConfig.class) // import test security configuration
public class ViewControllerTest {
    @Autowired // automatically inject the web application context dependency
    private WebApplicationContext context; // complete web app context for testing

    private MockMvc mockMvc; // test tool to simulate HTTP requests

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context) // set up MockMvc with the web application context
                .apply(springSecurity()) // apply Spring Security configuration
                .build();
    }

    @Test
    @DisplayName("Landing Page When Authenticated Redirects To Home")
    @WithMockUser // simulate authenticated user
    void landing_whenAuthenticated_shouldRedirectToHome() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"));
    }

    @Test
    @DisplayName("Landing Page When Not Authenticated Should Show Login")
    void landing_whenNotAuthenticated_shouldShowLogin() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    @DisplayName("Landing Page When Anonymous Should Show Login")
    @WithAnonymousUser // simulate anonymous user
    void landing_whenAnonymous_shouldShowLogin() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    @DisplayName("Login Page When Authenticated Redirects To Home")
    @WithMockUser
    void login_whenAuthenticated_shouldRedirectToHome() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"));
    }

    @Test
    @DisplayName("Login Page When Not Authenticated Should Show Login")
    void login_whenNotAuthenticated_shouldShowLogin() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    @DisplayName("Login Page When Anonymous Should Show Login")
    @WithAnonymousUser
    void login_whenAnonymous_shouldShowLogin() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    @DisplayName("Register Page When Authenticated Redirects To Home")
    @WithMockUser
    void register_whenAuthenticated_shouldRedirectToHome() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"));
    }

    @Test
    @DisplayName("Register Page When Not Authenticated Should Show Register")
    void register_whenNotAuthenticated_shouldShowRegister() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"));
    }

    @Test
    @DisplayName("Register Page When Anonymous Should Show Register")
    @WithAnonymousUser
    void register_whenAnonymous_shouldShowRegister() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"));
    }
}