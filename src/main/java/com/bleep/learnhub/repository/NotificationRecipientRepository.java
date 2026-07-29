package com.bleep.learnhub.repository;

import com.bleep.learnhub.entity.NotificationRecipient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificationRecipientRepository extends JpaRepository<NotificationRecipient, UUID>, JpaSpecificationExecutor<NotificationRecipient> {

    Page<NotificationRecipient> findByPartnerIdOrderByCreatedAtDesc(UUID partnerId, Pageable pageable);

    long countByPartnerIdAndIsReadFalse(UUID partnerId);

    long countByNotificationId(UUID notificationId);

    long countByNotificationIdAndIsReadTrue(UUID notificationId);

    Optional<NotificationRecipient> findByNotificationIdAndPartnerId(UUID notificationId, UUID partnerId);

    List<NotificationRecipient> findByNotificationId(UUID notificationId);

    @Modifying
    @Query("UPDATE NotificationRecipient nr SET nr.isRead = true, nr.readAt = :readAt WHERE nr.notification.id = :notificationId AND nr.partner.id = :partnerId")
    int markAsRead(@Param("notificationId") UUID notificationId, @Param("partnerId") UUID partnerId, @Param("readAt") LocalDateTime readAt);

    @Modifying
    @Query("UPDATE NotificationRecipient nr SET nr.isRead = true, nr.readAt = :readAt WHERE nr.partner.id = :partnerId AND nr.isRead = false")
    int markAllAsReadForPartner(@Param("partnerId") UUID partnerId, @Param("readAt") LocalDateTime readAt);

    void deleteByNotificationId(UUID notificationId);
}
