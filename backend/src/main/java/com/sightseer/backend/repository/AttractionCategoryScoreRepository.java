package com.sightseer.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.sightseer.backend.entity.AttractionCategoryScore;

public interface AttractionCategoryScoreRepository extends JpaRepository<AttractionCategoryScore, Long> {
    // Additional custom query methods can be defined here if needed

}