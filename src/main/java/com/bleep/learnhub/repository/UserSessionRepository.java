package com.bleep.learnhub.repository;

import com.bleep.learnhub.entity.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, UUID> {

    // Used to link a specific JWT token (JTI claim) to its database audit record
    Optional<UserSession> findBySessionId(String sessionId);

    // Get all currently active devices for a specific user
    List<UserSession> findByUserUsernameAndIsActiveTrue(String username);

    // Custom JPQL Query: Instantly revokes all sessions for an account.
    // Extremely useful for "Force Logout All Devices" or when an account is BLOCKED.
    @Modifying
    @Query("UPDATE UserSession s SET s.isActive = false, s.logoutAt = CURRENT_TIMESTAMP WHERE s.user.id = :userId AND s.isActive = true")
    void invalidateAllSessionsForUser(@Param("userId") UUID userId);

    @Modifying
    @Query("DELETE FROM UserSession s WHERE s.user.id = :userId")
    void deleteByUserId(@Param("userId") UUID userId);
}