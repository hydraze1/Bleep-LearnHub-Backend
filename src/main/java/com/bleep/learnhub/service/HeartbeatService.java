package com.bleep.learnhub.service;

import com.bleep.learnhub.sse.SseConnectionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class HeartbeatService {

    private final SseConnectionManager connectionManager;

    // Ping every 30 seconds to keep connection alive and detect dead sockets
    @Scheduled(fixedRate = 30000)
    public void sendHeartbeat() {
        if (connectionManager.getAllConnections().isEmpty()) {
            return;
        }

        connectionManager.getAllConnections().forEach((partnerId, emitter) -> {
            try {
                emitter.send(SseEmitter.event()
                        .name("ping")
                        .data("heartbeat"));
            } catch (IOException e) {
                log.warn("Heartbeat failed for partner {}. Removing dead connection.", partnerId);
                connectionManager.remove(partnerId, emitter);
            }
        });
    }
}
