package com.andydli.hivemind.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import com.andydli.hivemind.repository.UserRepository;
import com.andydli.hivemind.model.Profile;
import com.andydli.hivemind.dto.ProfileDTO;
import com.andydli.hivemind.dto.ProfileRequestDTO;
import com.andydli.hivemind.mapper.ProfileMapper;
import com.andydli.hivemind.model.User;
import com.andydli.hivemind.exceptions.ResourceNotFoundException;
import com.andydli.hivemind.exceptions.ProfileAlreadyExistsException;

import java.time.Instant;
import java.util.Set;
import java.util.HashSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProfileServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private ProfileMapper profileMapper;

    @InjectMocks
    private ProfileService profileService;

    @Mock
    private User user;

    @Mock
    private Profile profile;

    private final static Long USER_ID = 1L;

    private static final Long PROFILE_ID = 10L;
    private static final String PROFILE_BIO = "Test Bio";
    private static final String UPDATED_PROFILE_BIO = "Updated Test Bio";
    private static final Set<String> PROFILE_SKILLS = new HashSet<>();
    private static final Set<String> UPDATED_PROFILE_SKILLS = new HashSet<>(Set.of("Java", "Spring"));
    private static final int PROFILE_TOTAL_SESSIONS = 0;
    private static final double PROFILE_RATING = 0.0;


    @Test
    @DisplayName("Get Profile When User Does Not Exist Should Throw Exception")
    void getProfile_whenUserDoesNotExist_shouldThrowException() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> {
            profileService.getProfile(USER_ID);
        });

        assertEquals("User Not Found", ex.getMessage());
        verify(userRepository).findById(USER_ID);
        verify(profileMapper, never()).toDTO(any());
    }

    @Test
    @DisplayName("Get Profile When Profile Does Not Exist Should Throw Exception")
    void getProfile_whenProfileDoesNotExist_shouldThrowException() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(user.getProfile()).thenReturn(null);

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> {
            profileService.getProfile(USER_ID);
        });

        assertEquals("Profile Not Found", ex.getMessage());
        verify(userRepository).findById(USER_ID);
        verify(user).getProfile();
        verify(profileMapper, never()).toDTO(any());
    }

    @Test
    @DisplayName("Get Profile When Profile Exists Should Return Profile DTO")
    void getProfile_whenProfileExists_shouldReturnProfileDTO() {
        ProfileDTO expectedProfileDTO = new ProfileDTO(PROFILE_ID, PROFILE_BIO, PROFILE_SKILLS, PROFILE_TOTAL_SESSIONS, PROFILE_RATING, Instant.now(), Instant.now());

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(user.getProfile()).thenReturn(profile);
        when(profileMapper.toDTO(profile)).thenReturn(expectedProfileDTO);

        ProfileDTO result = profileService.getProfile(USER_ID);

        assertNotNull(result, "Returned Profile Should Not Be Null");
        assertEquals(expectedProfileDTO, result, "Returned Profile Should Match Expected");

        verify(userRepository).findById(USER_ID);
        verify(user).getProfile();
        verify(profileMapper).toDTO(profile);
    }

    @Test
    @DisplayName("Create Profile When User Does Not Exist Should Throw Exception")
    void createProfile_whenUserDoesNotExist_shouldThrowException() {
        ProfileRequestDTO profileRequestDTO = new ProfileRequestDTO(PROFILE_BIO, PROFILE_SKILLS);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> {
            profileService.createProfile(USER_ID, profileRequestDTO);
        });

        assertEquals("User Not Found", ex.getMessage());
        verify(userRepository).findById(USER_ID);
        verify(userRepository, never()).save(any());
        verify(profileMapper, never()).toDTO(any());
    }

    @Test
    @DisplayName("Create Profile When Profile Already Exists Should Throw Exception")
    void createProfile_whenProfileAlreadyExists_shouldThrowException() {
        ProfileRequestDTO profileRequestDTO = new ProfileRequestDTO(PROFILE_BIO, PROFILE_SKILLS);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(user.getProfile()).thenReturn(profile);

        ProfileAlreadyExistsException ex = assertThrows(ProfileAlreadyExistsException.class, () -> {
            profileService.createProfile(USER_ID, profileRequestDTO);
        });

        assertEquals("Profile Already Exists for this User", ex.getMessage());
        verify(userRepository).findById(USER_ID);
        verify(user).getProfile();
        verify(userRepository, never()).save(any());
        verify(profileMapper, never()).toDTO(any());
    }

    @Test
    @DisplayName("Create Profile When Valid Should Create Profile and Establish Bidirectional Relationship")
    void createProfile_whenValid_shouldCreateProfileAndEstablishBidirectionalRelationship() {
        ProfileRequestDTO profileRequestDTO = new ProfileRequestDTO(PROFILE_BIO, PROFILE_SKILLS);
        Profile newProfile = new Profile();
        ProfileDTO expectedProfileDTO = new ProfileDTO(PROFILE_ID, PROFILE_BIO, PROFILE_SKILLS, PROFILE_TOTAL_SESSIONS, PROFILE_RATING, Instant.now(), Instant.now());

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(user.getProfile()).thenReturn(null).thenReturn(newProfile);
        when(userRepository.save(user)).thenReturn(user);
        when(profileMapper.toDTO(newProfile)).thenReturn(expectedProfileDTO);

        ProfileDTO result = profileService.createProfile(USER_ID, profileRequestDTO);

        assertNotNull(result, "Returned Profile Should Not Be Null");
        assertEquals(expectedProfileDTO, result, "Returned Profile Should Match Expected");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).findById(USER_ID);
        verify(user, times(2)).getProfile();
        verify(user).setProfile(any(Profile.class));
        verify(userRepository).save(captor.capture());
        verify(profileMapper).toDTO(newProfile);

        User capturedUser = captor.getValue();
        assertEquals(user, capturedUser, "Persisted User Should Match Created User");
    }

    @Test
    @DisplayName("Update Profile When User Does Not Exist Should Throw Exception")
    void updateProfile_whenUserDoesNotExist_shouldThrowException() {
        ProfileRequestDTO profileRequestDTO = new ProfileRequestDTO(PROFILE_BIO, PROFILE_SKILLS);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> {
            profileService.updateProfile(USER_ID, profileRequestDTO);
        });

        assertEquals("User Not Found", ex.getMessage());
        verify(userRepository).findById(USER_ID);
        verify(userRepository, never()).save(any());
        verify(profileMapper, never()).toDTO(any());
    }

    @Test
    @DisplayName("Update Profile When Profile Does Not Exist Should Throw Exception")
    void updateProfile_whenProfileDoesNotExist_shouldThrowException() {
        ProfileRequestDTO profileRequestDTO = new ProfileRequestDTO(PROFILE_BIO, PROFILE_SKILLS);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(user.getProfile()).thenReturn(null);

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> {
            profileService.updateProfile(USER_ID, profileRequestDTO);
        });

        assertEquals("Profile Not Found. Create One First", ex.getMessage());
        verify(userRepository).findById(USER_ID);
        verify(user).getProfile();
        verify(userRepository, never()).save(any());
        verify(profileMapper, never()).toDTO(any());
    }

    @Test
    @DisplayName("Update Profile Should Update Bio and Skills")
    void updateProfile_shouldUpdateBioAndSkills() {
        ProfileDTO expectedProfileDTO = new ProfileDTO(PROFILE_ID, UPDATED_PROFILE_BIO, UPDATED_PROFILE_SKILLS, PROFILE_TOTAL_SESSIONS, PROFILE_RATING, Instant.now(), Instant.now());
        ProfileRequestDTO profileRequestDTO = new ProfileRequestDTO(UPDATED_PROFILE_BIO, UPDATED_PROFILE_SKILLS);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(user.getProfile()).thenReturn(profile);
        when(userRepository.save(user)).thenReturn(user);
        when(profileMapper.toDTO(profile)).thenReturn(expectedProfileDTO);

        ProfileDTO result = profileService.updateProfile(USER_ID, profileRequestDTO);

        assertNotNull(result, "Returned Profile Should Not Be Null");
        assertEquals(expectedProfileDTO, result, "Returned Profile Should Match Expected");

        verify(userRepository).findById(USER_ID);
        verify(user, times(2)).getProfile();
        verify(profile).setBio(UPDATED_PROFILE_BIO);
        verify(profile).setSkills(UPDATED_PROFILE_SKILLS);
        verify(userRepository).save(user);
        verify(profileMapper).toDTO(profile);
    }
}