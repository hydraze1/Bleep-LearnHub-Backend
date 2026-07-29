package com.bleep.learnhub.repository;

import com.bleep.learnhub.entity.StudentEnrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

@Repository
public interface StudentEnrollmentRepository
        extends JpaRepository<StudentEnrollment, UUID>, JpaSpecificationExecutor<StudentEnrollment> {
    List<StudentEnrollment> findByStudentId(UUID studentId);

    java.util.Optional<StudentEnrollment> findByStudentIdAndCourseIdAndBatchId(UUID studentId, UUID courseId,
            UUID batchId);

    @Query("SELECT DISTINCT e.studentId FROM StudentEnrollment e, Student s " +
           "WHERE e.studentId = s.id AND e.batchId = :batchId AND s.partnerId = :partnerId " +
           "AND e.isDeletedByPartner = false AND s.isDeletedByPartner = false")
    List<UUID> findStudentIdsByBatchIdAndPartnerId(@Param("batchId") UUID batchId, @Param("partnerId") UUID partnerId);
}
