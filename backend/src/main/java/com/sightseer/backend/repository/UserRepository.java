package com.sightseer.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.sightseer.backend.entity.User;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    // Additional custom query methods can be defined here if needed

    // find a user by their email
    Optional<User> findByEmail(String email);

    // does an email exist
    boolean existsByEmail(String email);
}
