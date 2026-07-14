package com.bleep.learnhub.repository;

import com.bleep.learnhub.entity.StudentSessionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface StudentSessionLogRepository extends JpaRepository<StudentSessionLog, UUID> {
    @Query("SELECT COUNT(DISTINCT ssl.studentId) FROM StudentSessionLog ssl, Student s " +
           "WHERE ssl.studentId = s.id AND ssl.sessionId = :sessionId AND s.partnerId = :partnerId")
    long countDistinctStudentsBySessionIdAndPartnerId(@Param("sessionId") UUID sessionId, @Param("partnerId") UUID partnerId);
}
