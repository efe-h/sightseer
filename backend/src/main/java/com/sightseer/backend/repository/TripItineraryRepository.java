package com.sightseer.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.sightseer.backend.entity.TripItinerary;

public interface TripItineraryRepository extends JpaRepository<TripItinerary, Long> {
    // Additional custom query methods can be defined here if needed

}