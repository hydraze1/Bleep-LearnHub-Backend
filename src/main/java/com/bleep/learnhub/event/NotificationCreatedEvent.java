package com.bleep.learnhub.event;

import com.bleep.learnhub.entity.Notification;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class NotificationCreatedEvent {
    private final Notification notification;
    private final List<UUID> recipientPartnerIds;
}
