package com.bleep.learnhub.repository;

import com.bleep.learnhub.entity.PartnerAccessRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PartnerAccessRequestRepository extends JpaRepository<PartnerAccessRequest, UUID> {
    List<PartnerAccessRequest> findByVendorId(UUID vendorId);
    List<PartnerAccessRequest> findByPartnerId(UUID partnerId);
}
