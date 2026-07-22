package com.bleep.learnhub.repository;

import com.bleep.learnhub.entity.Batch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BatchRepository extends JpaRepository<Batch, UUID>, JpaSpecificationExecutor<Batch> {
    long countByCourseId(UUID courseId);
    List<Batch> findByCourseId(UUID courseId);
    List<Batch> findByCourseIdIn(List<UUID> courseIds);
}
