package com.andydli.hivemind.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.andydli.hivemind.model.Portal;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PortalRepository extends JpaRepository<Portal, Long> {
    List<Portal> findByCreatorId(Long creatorId);

    @Query("SELECT p FROM Portal p JOIN FETCH p.creator ORDER BY p.createdAt DESC")
    List<Portal> findAllWithCreators();
}