package com.bleep.learnhub.repository;

import com.bleep.learnhub.entity.StudentEnrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface StudentEnrollmentRepository extends JpaRepository<StudentEnrollment, UUID> {
    List<StudentEnrollment> findByStudentId(UUID studentId);
}
