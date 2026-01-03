package com.andydli.hivemind.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.andydli.hivemind.model.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    // fetch user with portals and profile relationships eagerly; specific for displaying home page user info
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.portals LEFT JOIN FETCH u.profile WHERE u.id = :id")
    Optional<User> findByIdWithRelationships(@Param("id") Long id);
}