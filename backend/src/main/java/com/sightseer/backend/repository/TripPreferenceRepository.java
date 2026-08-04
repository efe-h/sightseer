package com.sightseer.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.sightseer.backend.entity.TripPreference;

public interface TripPreferenceRepository extends JpaRepository<TripPreference, Long> {
    // Additional custom query methods can be defined here if needed

}