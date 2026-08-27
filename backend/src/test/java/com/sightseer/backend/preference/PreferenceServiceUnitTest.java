package com.sightseer.backend.preference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sightseer.backend.entity.User;
import com.sightseer.backend.entity.UserPreference;
import com.sightseer.backend.exception.PreferenceNotFoundException;
import com.sightseer.backend.exception.UserNotFoundException;
import com.sightseer.backend.preference.dto.PreferenceRequest;
import com.sightseer.backend.preference.dto.PreferenceResponse;
import com.sightseer.backend.repository.UserPreferenceRepository;
import com.sightseer.backend.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class PreferenceServiceUnitTest {

    @Mock
    private UserPreferenceRepository userPreferenceRepository;

    @Mock
    private UserRepository userRepository;

    private PreferenceService preferenceService;

    @BeforeEach
    void setUp() {
        preferenceService = new PreferenceService(
                userPreferenceRepository,
                userRepository);
    }

    @Test
    void savePreferencesCreatesPreferencesForExistingUser() {
        Long userId = 4L;
        User user = user(userId);
        PreferenceRequest request = request(1, 2, 3, 4, 5, 1, 2, 3, 4, 5);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userPreferenceRepository.findById(userId)).thenReturn(Optional.empty());
        when(userPreferenceRepository.save(any(UserPreference.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PreferenceResponse response = preferenceService.savePreferences(userId, request);

        ArgumentCaptor<UserPreference> preferenceCaptor = ArgumentCaptor.forClass(UserPreference.class);
        verify(userPreferenceRepository).save(preferenceCaptor.capture());

        UserPreference capturedPreference = preferenceCaptor.getValue();
        assertEquals(user, capturedPreference.getUser());
        assertEquals(Short.valueOf((short) 1), capturedPreference.getHistory());
        assertEquals(Short.valueOf((short) 5), capturedPreference.getScience());
        assertEquals(Short.valueOf((short) 5), capturedPreference.getFamily());
        assertResponseEquals(response, 1, 2, 3, 4, 5, 1, 2, 3, 4, 5);
    }

    @Test
    void savePreferencesUpdatesExistingPreferences() {
        Long userId = 4L;
        User user = user(userId);
        UserPreference existingPreference = preference(user, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1);
        PreferenceRequest request = request(5, 4, 3, 2, 1, 5, 4, 3, 2, 1);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userPreferenceRepository.findById(userId))
                .thenReturn(Optional.of(existingPreference));
        when(userPreferenceRepository.save(any(UserPreference.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PreferenceResponse response = preferenceService.savePreferences(userId, request);

        verify(userPreferenceRepository).save(existingPreference);
        assertResponseEquals(response, 5, 4, 3, 2, 1, 5, 4, 3, 2, 1);
    }

    @Test
    void savePreferencesThrowsWhenUserDoesNotExist() {
        Long userId = 99L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> preferenceService.savePreferences(
                        userId,
                        request(1, 1, 1, 1, 1, 1, 1, 1, 1, 1)));

        verify(userPreferenceRepository, never()).findById(any(Long.class));
        verify(userPreferenceRepository, never()).save(any(UserPreference.class));
    }

    @Test
    void getPreferencesReturnsStoredPreferences() {
        Long userId = 4L;
        UserPreference storedPreference = preference(
                user(userId),
                2, 3, 4, 5, 1, 2, 3, 4, 5, 1);
        when(userPreferenceRepository.findById(userId))
                .thenReturn(Optional.of(storedPreference));

        PreferenceResponse response = preferenceService.getPreferences(userId);

        assertResponseEquals(response, 2, 3, 4, 5, 1, 2, 3, 4, 5, 1);
    }

    @Test
    void getPreferencesThrowsWhenPreferencesDoNotExist() {
        Long userId = 99L;
        when(userPreferenceRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(
                PreferenceNotFoundException.class,
                () -> preferenceService.getPreferences(userId));
    }

    private PreferenceRequest request(
            int history,
            int art,
            int architecture,
            int nature,
            int science,
            int food,
            int entertainment,
            int shopping,
            int views,
            int family) {
        return new PreferenceRequest(
                history,
                art,
                architecture,
                nature,
                science,
                food,
                entertainment,
                shopping,
                views,
                family);
    }

    private UserPreference preference(
            User user,
            int history,
            int art,
            int architecture,
            int nature,
            int science,
            int food,
            int entertainment,
            int shopping,
            int views,
            int family) {
        UserPreference preference = new UserPreference();
        preference.setUser(user);
        preference.setHistory((short) history);
        preference.setArt((short) art);
        preference.setArchitecture((short) architecture);
        preference.setNature((short) nature);
        preference.setScience((short) science);
        preference.setFood((short) food);
        preference.setEntertainment((short) entertainment);
        preference.setShopping((short) shopping);
        preference.setViews((short) views);
        preference.setFamily((short) family);
        return preference;
    }

    private User user(Long id) {
        User user = new User();
        user.setId(id);
        return user;
    }

    private void assertResponseEquals(
            PreferenceResponse response,
            int history,
            int art,
            int architecture,
            int nature,
            int science,
            int food,
            int entertainment,
            int shopping,
            int views,
            int family) {
        assertEquals(history, response.history());
        assertEquals(art, response.art());
        assertEquals(architecture, response.architecture());
        assertEquals(nature, response.nature());
        assertEquals(science, response.science());
        assertEquals(food, response.food());
        assertEquals(entertainment, response.entertainment());
        assertEquals(shopping, response.shopping());
        assertEquals(views, response.views());
        assertEquals(family, response.family());
    }
}
