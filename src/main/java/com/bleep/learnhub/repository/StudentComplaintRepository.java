package com.bleep.learnhub.repository;

import com.bleep.learnhub.entity.StudentComplaint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface StudentComplaintRepository extends JpaRepository<StudentComplaint, UUID>, JpaSpecificationExecutor<StudentComplaint> {
    List<StudentComplaint> findByVendorId(UUID vendorId);
    List<StudentComplaint> findTop5ByVendorIdOrderByCreatedAtDesc(UUID vendorId);
    List<StudentComplaint> findByPartnerId(UUID partnerId);
    List<StudentComplaint> findTop5ByPartnerIdOrderByCreatedAtDesc(UUID partnerId);
}
