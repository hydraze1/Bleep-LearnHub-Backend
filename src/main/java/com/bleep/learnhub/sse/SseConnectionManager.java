package com.bleep.learnhub.sse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class SseConnectionManager {

    // Emitter timeout: 1 hour (3,600,000 ms)
    private static final Long EMITTER_TIMEOUT = 3600000L;

    private final Map<UUID, SseEmitter> activeConnections = new ConcurrentHashMap<>();

    public SseEmitter register(UUID partnerId) {
        // Complete and clean up any existing connection first
        SseEmitter oldEmitter = activeConnections.remove(partnerId);
        if (oldEmitter != null) {
            try {
                oldEmitter.complete();
                log.info("🧹 Cleaned up and completed old SSE connection for partner: {}", partnerId);
            } catch (Exception e) {
                log.warn("⚠️ Failed to cleanly complete old SSE connection for partner {}: {}", partnerId, e.getMessage());
            }
        }

        SseEmitter emitter = new SseEmitter(EMITTER_TIMEOUT);

        emitter.onCompletion(() -> {
            log.info("📡 SSE Connection completed for partner: {}", partnerId);
            remove(partnerId, emitter);
        });

        emitter.onTimeout(() -> {
            log.info("⏰ SSE Connection timed out for partner: {}", partnerId);
            remove(partnerId, emitter);
        });

        emitter.onError(e -> {
            log.warn("⚠️ SSE Connection error for partner {}: {}", partnerId, e.getMessage());
            remove(partnerId, emitter);
        });

        activeConnections.put(partnerId, emitter);
        log.info("✅ Registered SSE connection for partner: {}. Total active connections: {}", partnerId, activeConnections.size());
        
        // Initial handshake event
        try {
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data("SSE Connection established successfully for partner: " + partnerId));
        } catch (IOException e) {
            log.warn("Failed to send initial handshake SSE event to partner: {}", partnerId);
            remove(partnerId, emitter);
        }

        return emitter;
    }

    public void remove(UUID partnerId, SseEmitter emitter) {
        if (emitter != null) {
            activeConnections.remove(partnerId, emitter);
        } else {
            activeConnections.remove(partnerId);
        }
    }

    public boolean isConnected(UUID partnerId) {
        return activeConnections.containsKey(partnerId);
    }

    public void send(UUID partnerId, Object data, String eventName) {
        SseEmitter emitter = activeConnections.get(partnerId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name(eventName)
                        .data(data));
            } catch (IOException e) {
                log.warn("Failed to send SSE event to partner {}. Removing dead connection.", partnerId);
                remove(partnerId, emitter);
            }
        }
    }

    public Map<UUID, SseEmitter> getAllConnections() {
        return activeConnections;
    }
}
