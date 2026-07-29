package com.bleep.learnhub.controller;

import com.bleep.learnhub.entity.Partner;
import com.bleep.learnhub.exception.ResourceNotFoundException;
import com.bleep.learnhub.repository.PartnerRepository;
import com.bleep.learnhub.sse.SseConnectionManager;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.security.Principal;

@RestController
@RequestMapping("/partners/notifications")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('PARTNER')")
public class PartnerNotificationStreamController {

    private final SseConnectionManager sseConnectionManager;
    private final PartnerRepository partnerRepository;

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamNotifications(Principal principal) {
        Partner partner = partnerRepository.findByUserUsername(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Partner not found for username: " + principal.getName()));

        return sseConnectionManager.register(partner.getId());
    }
}
