package com.bleep.learnhub.repository;

import com.bleep.learnhub.entity.ApiLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ApiLogRepository extends JpaRepository<ApiLog, UUID> {
}
