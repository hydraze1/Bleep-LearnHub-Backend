package com.bleep.learnhub.repository;

import com.bleep.learnhub.entity.Partner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PartnerRepository extends JpaRepository<Partner, UUID>, JpaSpecificationExecutor<Partner> {

    // Used when a Partner accesses their own profile
    Optional<Partner> findByUserUsername(String username);

    // Used when a Vendor wants to see a list of all their onboarded Partners
    // Translates to: JOIN vendor v JOIN users u (vendor's user) WHERE u.username = ?
    List<Partner> findByVendorUserUsername(String vendorUsername);
    
    // Alternatively, if you already have the Vendor's UUID
    List<Partner> findByVendorId(UUID vendorId);

    long countByVendorId(UUID vendorId);
}
