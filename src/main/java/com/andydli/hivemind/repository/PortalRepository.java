package com.andydli.hivemind.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.andydli.hivemind.model.Portal;
import com.andydli.hivemind.model.User;

import java.util.List;

public interface PortalRepository extends JpaRepository<Portal, Long> {
    List<Portal> findByCreatorId(Long creatorId);
}