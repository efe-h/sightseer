package com.sightseer.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.sightseer.backend.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    // Additional custom query methods can be defined here if needed

}