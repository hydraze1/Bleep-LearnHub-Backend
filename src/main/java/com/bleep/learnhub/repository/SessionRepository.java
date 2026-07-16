package com.bleep.learnhub.repository;

import com.bleep.learnhub.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface SessionRepository extends JpaRepository<Session, UUID> {
    long countByBatchId(UUID batchId);
    List<Session> findByBatchIdOrderBySequenceOrderAsc(UUID batchId);
    List<Session> findByBatchIdOrderByScheduledDateAsc(UUID batchId);

    @Query("SELECT COALESCE(MAX(s.sequenceOrder), 0) FROM Session s WHERE s.batchId = :batchId")
    Integer findMaxSequenceOrderByBatchId(@Param("batchId") UUID batchId);

    @Query("SELECT s FROM Session s WHERE s.batchId IN :batchIds AND s.scheduledDate >= :fromDate AND s.scheduledDate <= :toDate ORDER BY s.scheduledDate ASC, s.scheduledTime ASC")
    List<Session> findByBatchIdsAndScheduledDateBetween(
        @Param("batchIds") List<UUID> batchIds, 
        @Param("fromDate") LocalDate fromDate, 
        @Param("toDate") LocalDate toDate
    );
}
