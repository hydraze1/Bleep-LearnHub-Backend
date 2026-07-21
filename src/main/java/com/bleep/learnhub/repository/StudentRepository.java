package com.bleep.learnhub.repository;

import com.bleep.learnhub.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface StudentRepository extends JpaRepository<Student, UUID>, JpaSpecificationExecutor<Student> {
    List<Student> findByPartnerId(UUID partnerId);
    long countByPartnerId(UUID partnerId);
    long countByPartnerIdIn(List<UUID> partnerIds);
    java.util.Optional<Student> findByEmail(String email);
}
