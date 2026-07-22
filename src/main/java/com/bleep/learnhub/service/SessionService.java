package com.bleep.learnhub.service;

import com.bleep.learnhub.dto.request.SessionCreateDto;
import com.bleep.learnhub.dto.request.SessionReorderRequestDto;
import com.bleep.learnhub.dto.request.SessionUpdateDto;
import com.bleep.learnhub.dto.response.SessionDataDto;
import com.bleep.learnhub.dto.response.SessionProfileResponseDto;
import com.bleep.learnhub.entity.Session;
import com.bleep.learnhub.exception.ResourceNotFoundException;
import com.bleep.learnhub.repository.BatchRepository;
import com.bleep.learnhub.repository.CourseRepository;
import com.bleep.learnhub.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final SessionRepository sessionRepository;
    private final BatchRepository batchRepository;
    private final CourseRepository courseRepository;

    public SessionProfileResponseDto createSession(SessionCreateDto dto) {
        if (!courseRepository.existsById(dto.getCourseId())) {
            throw new ResourceNotFoundException("Course not found with id: " + dto.getCourseId());
        }
        if (!batchRepository.existsById(dto.getBatchId())) {
            throw new ResourceNotFoundException("Batch not found with id: " + dto.getBatchId());
        }

        Integer maxSequenceOrder = sessionRepository.findMaxSequenceOrderByBatchId(dto.getBatchId());

        Session session = Session.builder()
                .courseId(dto.getCourseId())
                .batchId(dto.getBatchId())
                .sessionType(dto.getSessionType())
                .title(dto.getTitle())
                .subtitle(dto.getSubtitle())
                .description(dto.getDescription())
                .liveLink(dto.getLiveLink())
                .recordedLink(dto.getRecordedLink())
                .resourceLink(dto.getResourceLink())
                .sequenceOrder(maxSequenceOrder + 1)
                .scheduledDate(dto.getScheduledDate())
                .scheduledTime(dto.getScheduledTime() != null ? java.time.LocalTime.parse(dto.getScheduledTime()) : null)
                .build();

        session = sessionRepository.save(session);
        return mapToProfileDto(session);
    }

    public void updateSession(UUID id, SessionUpdateDto dto) {
        Session session = getSessionEntity(id);

        session.setSessionType(dto.getSessionType());
        session.setTitle(dto.getTitle());
        session.setSubtitle(dto.getSubtitle());
        session.setDescription(dto.getDescription());
        session.setLiveLink(dto.getLiveLink());
        session.setRecordedLink(dto.getRecordedLink());
        session.setResourceLink(dto.getResourceLink());
        session.setScheduledDate(dto.getScheduledDate());
        session.setScheduledTime(dto.getScheduledTime() != null ? java.time.LocalTime.parse(dto.getScheduledTime()) : null);

        sessionRepository.save(session);
    }

    public void deleteSession(UUID id) {
        Session session = getSessionEntity(id);
        sessionRepository.delete(session);
    }

    @Transactional
    public void reorderSessions(SessionReorderRequestDto request) {
        // Fetch all sessions belonging to the specified batch
        List<Session> sessionsToUpdate = sessionRepository.findByBatchIdOrderBySequenceOrderAsc(request.getBatchId());

        // Create a Map for quick lookup (ID -> Session)
        Map<UUID, Session> sessionMap = sessionsToUpdate.stream()
                .collect(Collectors.toMap(Session::getId, session -> session));

        // Loop through the requested ID order and apply the new sequence order
        for (SessionReorderRequestDto.SessionOrderItem item : request.getSessionIds()) {
            UUID sessionId = item.getId();
            Session session = sessionMap.get(sessionId);
            if (session != null) {
                // Verify that the session belongs to the specified course
                if (!session.getCourseId().equals(request.getCourseId())) {
                    throw new RuntimeException("Session " + sessionId + " does not belong to course " + request.getCourseId());
                }
                session.setSequenceOrder(item.getIndex());
            } else {
                throw new RuntimeException("Session not found with ID: " + sessionId + " in Batch: " + request.getBatchId());
            }
        }

        // Save them all back to the database in one bulk action
        sessionRepository.saveAll(sessionsToUpdate);
    }

    public List<SessionDataDto> getAllSessions(UUID courseId, UUID batchId, String type, String search, String sortOrder) {
        List<Session> sessions;
        if (batchId != null) {
            sessions = sessionRepository.findByBatchIdOrderBySequenceOrderAsc(batchId);
        } else {
            sessions = sessionRepository.findAll();
        }
        
        return sessions.stream()
                .filter(s -> courseId == null || courseId.equals(s.getCourseId()))
                .filter(s -> type == null || (s.getSessionType() != null && s.getSessionType().name().equalsIgnoreCase(type)))
                .filter(s -> search == null || (s.getTitle() != null && s.getTitle().toLowerCase().contains(search.toLowerCase())))
                .sorted((s1, s2) -> {
                    if ("desc".equalsIgnoreCase(sortOrder)) {
                        return s2.getSequenceOrder().compareTo(s1.getSequenceOrder());
                    }
                    return s1.getSequenceOrder().compareTo(s2.getSequenceOrder());
                })
                .map(this::mapToDataDto)
                .collect(Collectors.toList());
    }

    public SessionProfileResponseDto getSessionById(UUID id) {
        return mapToProfileDto(getSessionEntity(id));
    }

    private Session getSessionEntity(UUID id) {
        return sessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found with id: " + id));
    }

    private SessionDataDto mapToDataDto(Session session) {
        return SessionDataDto.builder()
                .id(session.getId())
                .courseId(session.getCourseId())
                .batchId(session.getBatchId())
                .sessionType(session.getSessionType() != null ? session.getSessionType().name() : null)
                .title(session.getTitle())
                .subtitle(session.getSubtitle())
                .description(session.getDescription())
                .liveLink(session.getLiveLink())
                .recordedLink(session.getRecordedLink())
                .resourceLink(session.getResourceLink())
                .sequenceOrder(session.getSequenceOrder())
                .scheduledDate(session.getScheduledDate() != null ? session.getScheduledDate().toString() : null)
                .scheduledTime(session.getScheduledTime() != null ? session.getScheduledTime().toString() : null)
                .createdAt(session.getCreatedAt() != null ? session.getCreatedAt().toString() : null)
                .updatedAt(session.getUpdatedAt() != null ? session.getUpdatedAt().toString() : null)
                .build();
    }

    private SessionProfileResponseDto mapToProfileDto(Session session) {
        return SessionProfileResponseDto.builder()
                .id(session.getId())
                .courseId(session.getCourseId())
                .batchId(session.getBatchId())
                .sessionType(session.getSessionType() != null ? session.getSessionType().name() : null)
                .title(session.getTitle())
                .subtitle(session.getSubtitle())
                .description(session.getDescription())
                .liveLink(session.getLiveLink())
                .recordedLink(session.getRecordedLink())
                .resourceLink(session.getResourceLink())
                .sequenceOrder(session.getSequenceOrder())
                .scheduledDate(session.getScheduledDate() != null ? session.getScheduledDate().toString() : null)
                .scheduledTime(session.getScheduledTime() != null ? session.getScheduledTime().toString() : null)
                .createdAt(session.getCreatedAt() != null ? session.getCreatedAt().toString() : null)
                .updatedAt(session.getUpdatedAt() != null ? session.getUpdatedAt().toString() : null)
                .build();
    }
}
