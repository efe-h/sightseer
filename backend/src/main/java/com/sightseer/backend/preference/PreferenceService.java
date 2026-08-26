package com.sightseer.backend.preference;

import com.sightseer.backend.preference.dto.PreferenceResponse;
import com.sightseer.backend.preference.dto.PreferenceRequest;
import com.sightseer.backend.repository.UserRepository;
import com.sightseer.backend.repository.UserPreferenceRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.sightseer.backend.entity.User;
import com.sightseer.backend.exception.UserNotFoundException;
import com.sightseer.backend.entity.UserPreference;
import com.sightseer.backend.exception.PreferenceNotFoundException;

@Service
public class PreferenceService {
    private final UserPreferenceRepository userPreferenceRepository;
    private final UserRepository userRepository;

    public PreferenceService(UserPreferenceRepository userPreferenceRepository, UserRepository userRepository) {
        this.userPreferenceRepository = userPreferenceRepository;
        this.userRepository = userRepository;
    }

    // to preference response
    private PreferenceResponse toPreferenceResponse(UserPreference userPreference) {
        return new PreferenceResponse(
                userPreference.getHistory().intValue(),
                userPreference.getArt().intValue(),
                userPreference.getArchitecture().intValue(),
                userPreference.getNature().intValue(),
                userPreference.getScience().intValue(),
                userPreference.getFood().intValue(),
                userPreference.getEntertainment().intValue(),
                userPreference.getShopping().intValue(),
                userPreference.getViews().intValue(),
                userPreference.getFamily().intValue());
    }

    // save preferences for a user
    @Transactional
    public PreferenceResponse savePreferences(Long userId, PreferenceRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException());
        UserPreference userPreference = userPreferenceRepository.findById(userId).orElseGet(() -> {
            UserPreference newPreference = new UserPreference();
            newPreference.setUser(user);
            return newPreference;
        });

        userPreference.setHistory(request.history().shortValue());
        userPreference.setArt(request.art().shortValue());
        userPreference.setArchitecture(request.architecture().shortValue());
        userPreference.setNature(request.nature().shortValue());
        userPreference.setScience(request.science().shortValue());
        userPreference.setFood(request.food().shortValue());
        userPreference.setEntertainment(request.entertainment().shortValue());
        userPreference.setShopping(request.shopping().shortValue());
        userPreference.setViews(request.views().shortValue());
        userPreference.setFamily(request.family().shortValue());

        UserPreference savedPreference = userPreferenceRepository.save(userPreference);

        return toPreferenceResponse(savedPreference);
    }

    // get preferences for a user
    @Transactional(readOnly = true)
    public PreferenceResponse getPreferences(Long userId) {
        UserPreference userPreference = userPreferenceRepository.findById(userId)
                .orElseThrow(() -> new PreferenceNotFoundException());

        return toPreferenceResponse(userPreference);
    }
}
