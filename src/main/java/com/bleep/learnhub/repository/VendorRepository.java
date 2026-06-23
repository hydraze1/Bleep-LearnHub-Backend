package com.bleep.learnhub.repository;

import com.bleep.learnhub.entity.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface VendorRepository extends JpaRepository<Vendor, UUID> {
    
    // Spring Data JPA translates this to: 
    // SELECT v.* FROM vendors v JOIN users u ON v.user_id = u.id WHERE u.username = ?
    Optional<Vendor> findByUserUsername(String username);
    
}