package com.andydli.hivemind.service;

import com.andydli.hivemind.dto.UserDTO;
import com.andydli.hivemind.dto.UserLoginDTO;
import com.andydli.hivemind.dto.UserRegistrationDTO;
import com.andydli.hivemind.exceptions.*;
import com.andydli.hivemind.mapper.UserMapper;
import com.andydli.hivemind.model.User;
import com.andydli.hivemind.repository.UserRepository;
import com.andydli.hivemind.security.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.ArrayList;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private UserService userService;

    @Mock
    private User user;

    private static final Long USER_ID = 1L;
    private static final String USER_EMAIL = "test@example.com";
    private static final String USER_UNNORMALIZED_EMAIL = "  tEsT@exAMpLE.CoM ";
    private static final String USER_UNKNOWN_EMAIL = "unknown@example.com";
    private static final String USER_PLAIN_PASSWORD = "12345678";
    private static final String USER_PLAIN_WRONG_PASSWORD = "87654321";
    private static final String USER_FIRST_NAME = "Test";
    private static final String USER_LAST_NAME = "User";

    @Test
    @DisplayName("Registering a User When Passwords Do Not Match Should Throw Exception")
    void registerUser_whenPasswordsDoNotMatch_shouldThrowException() {
        UserRegistrationDTO mismatchedPasswordDTO = new UserRegistrationDTO(
                USER_EMAIL, USER_PLAIN_PASSWORD, USER_PLAIN_WRONG_PASSWORD, USER_FIRST_NAME, USER_LAST_NAME
        );

        PasswordMismatchException ex = assertThrows(PasswordMismatchException.class, () -> {
            userService.registerUser(mismatchedPasswordDTO);
        });
        assertEquals("Passwords Do Not Match", ex.getMessage());
        verify(userRepository, never()).existsByEmail(USER_EMAIL);
        verify(userMapper, never()).toEntity(any());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Registering a User When Email Already Exists Should Throw Exception")
    void registerUser_whenEmailAlreadyExists_shouldThrowException() {
        when(userRepository.existsByEmail(USER_EMAIL)).thenReturn(true);

        UserRegistrationDTO dto = new UserRegistrationDTO(
                USER_EMAIL, USER_PLAIN_PASSWORD, USER_PLAIN_PASSWORD, USER_FIRST_NAME, USER_LAST_NAME
        );

        EmailAlreadyExistsException ex = assertThrows(EmailAlreadyExistsException.class, () -> {
            userService.registerUser(dto);
        });
        assertEquals("Email Already Exists", ex.getMessage());
        verify(userMapper, never()).toEntity(any());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Registering a User Should Normalize Email, Hash Password, and Save User")
    void registerUser_shouldHashPassword_normalizeEmail_saveUser() {
        UserRegistrationDTO dto = new UserRegistrationDTO(
                USER_UNNORMALIZED_EMAIL, USER_PLAIN_PASSWORD, USER_PLAIN_PASSWORD, USER_FIRST_NAME, USER_LAST_NAME
        );

        UserDTO expectedDTO = new UserDTO(
                USER_ID, USER_EMAIL, USER_FIRST_NAME, USER_LAST_NAME, new ArrayList<>(), null, Instant.now(), Instant.now()
        );

        when(userMapper.toEntity(dto)).thenReturn(new User());
        when(passwordEncoder.encode(USER_PLAIN_PASSWORD)).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
           User u = invocation.getArgument(0);
           u.setId(USER_ID);
           return u;
        });
        when(userMapper.toDTO(any(User.class))).thenReturn(expectedDTO);

        UserDTO savedUserDTO = userService.registerUser(dto);

        assertNotNull(savedUserDTO, "Saved User Should Not Be Null");
        assertEquals(USER_ID, savedUserDTO.id(), "Saved User Should Have Set ID");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User persisted = captor.getValue(); // before saving

        assertEquals(USER_EMAIL, persisted.getEmail(), "Email Should Be Normalized Pre-Save");
        verify(userRepository).existsByEmail(USER_EMAIL);
        verify(userMapper).toEntity(dto);
        verify(passwordEncoder).encode(USER_PLAIN_PASSWORD);
        verify(userMapper).toDTO(any(User.class));
    }

    @Test
    @DisplayName("Authenticating User With Valid Credentials Should Return JWT Token")
    void authenticateUser_withValidCredentials_shouldReturnJwtToken() {
        UserLoginDTO loginDTO = new UserLoginDTO(USER_EMAIL, USER_PLAIN_PASSWORD);

        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
        when(user.verifyPassword(passwordEncoder, USER_PLAIN_PASSWORD)).thenReturn(true);
        when(jwtService.generateToken(USER_ID, USER_EMAIL)).thenReturn("jwtToken");
        when(user.getId()).thenReturn(USER_ID);
        when(user.getEmail()).thenReturn(USER_EMAIL);

        String token = userService.authenticateUser(loginDTO);

        assertNotNull(token, "Token Should Not Be Null");
        assertEquals("jwtToken", token, "Returned Token Should Match Expected JWT Token");
        verify(userRepository).findByEmail(USER_EMAIL);
        verify(user).verifyPassword(passwordEncoder, USER_PLAIN_PASSWORD);
        verify(jwtService).generateToken(USER_ID, USER_EMAIL);
    }

    @Test
    @DisplayName("Authenticating User With Invalid Email Should Throw Exception")
    void authenticateUser_withInvalidEmail_shouldThrowException() {
        UserLoginDTO loginDTO = new UserLoginDTO(USER_UNKNOWN_EMAIL, USER_PLAIN_PASSWORD);

        when(userRepository.findByEmail(USER_UNKNOWN_EMAIL)).thenReturn(Optional.empty());

        InvalidCredentialsException ex = assertThrows(InvalidCredentialsException.class, () -> {
            userService.authenticateUser(loginDTO);
        });

        assertEquals("Invalid Email or Password", ex.getMessage());
        verify(userRepository).findByEmail(USER_UNKNOWN_EMAIL);
        verify(user, never()).verifyPassword(any(), anyString());
        verify(jwtService, never()).generateToken(anyLong(), anyString());
    }

    @Test
    @DisplayName("Authenticating User With Invalid Password Should Throw Exception")
    void authenticateUser_withInvalidPassword_shouldThrowException() {
        UserLoginDTO loginDTO = new UserLoginDTO(USER_EMAIL, USER_PLAIN_WRONG_PASSWORD);

        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
        when(user.verifyPassword(passwordEncoder, USER_PLAIN_WRONG_PASSWORD)).thenReturn(false);

        InvalidCredentialsException ex = assertThrows(InvalidCredentialsException.class, () -> {
            userService.authenticateUser(loginDTO);
        });

        assertEquals("Invalid Email or Password", ex.getMessage());
        verify(userRepository).findByEmail(USER_EMAIL);
        verify(user).verifyPassword(passwordEncoder, USER_PLAIN_WRONG_PASSWORD);
        verify(jwtService, never()).generateToken(anyLong(), anyString());
    }

    @Test
    @DisplayName("Authenticating User Should Normalize Email Before Lookup")
    void authenticateUser_shouldNormalizeEmailBeforeLookup() {
        UserLoginDTO loginDTO = new UserLoginDTO(USER_UNNORMALIZED_EMAIL, USER_PLAIN_PASSWORD);

        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
        when(user.verifyPassword(passwordEncoder, USER_PLAIN_PASSWORD)).thenReturn(true);
        when(jwtService.generateToken(USER_ID, USER_EMAIL)).thenReturn("jwtToken");
        when(user.getId()).thenReturn(USER_ID);
        when(user.getEmail()).thenReturn(USER_EMAIL);

        userService.authenticateUser(loginDTO);

        verify(userRepository).findByEmail(USER_EMAIL);
        verify(user).verifyPassword(passwordEncoder, USER_PLAIN_PASSWORD);
        verify(jwtService).generateToken(USER_ID, USER_EMAIL);
    }
}