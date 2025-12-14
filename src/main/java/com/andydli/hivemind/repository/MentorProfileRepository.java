package com.andydli.hivemind.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.andydli.hivemind.model.MentorProfile;

import java.util.Optional;
import java.util.List;

public interface MentorProfileRepository extends JpaRepository<MentorProfile, Long> {
    Optional<MentorProfile> findByUser_Id(Long userId);
}