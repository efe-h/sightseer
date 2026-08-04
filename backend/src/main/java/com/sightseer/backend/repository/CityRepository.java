package com.sightseer.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.sightseer.backend.entity.City;

public interface CityRepository extends JpaRepository<City, Long> {
    // Additional custom query methods can be defined here if needed

}