package com.bleep.learnhub.repository;

import com.bleep.learnhub.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SessionRepository extends JpaRepository<Session, UUID> {
    long countByBatchId(UUID batchId);
    List<Session> findByBatchIdOrderBySequenceOrderAsc(UUID batchId);
    List<Session> findByBatchIdOrderByScheduledDateAsc(UUID batchId);
}
