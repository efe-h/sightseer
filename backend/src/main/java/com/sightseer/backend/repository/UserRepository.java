package com.sightseer.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.sightseer.backend.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
    // Additional custom query methods can be defined here if needed

}
