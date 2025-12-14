package com.andydli.hivemind.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.andydli.hivemind.model.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}