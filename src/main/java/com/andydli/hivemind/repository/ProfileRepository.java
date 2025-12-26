package com.andydli.hivemind.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.andydli.hivemind.model.Profile;

import java.util.Optional;

public interface ProfileRepository extends JpaRepository<Profile, Long> {
    Optional<Profile> findByUser_Id(Long userId);
}