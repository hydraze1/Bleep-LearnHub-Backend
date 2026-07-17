package com.bleep.learnhub.service;

import com.bleep.learnhub.dto.response.BatchDataDto;
import com.bleep.learnhub.dto.response.CourseDataDto;
import com.bleep.learnhub.dto.response.PartnerAccessSessionDto;
import com.bleep.learnhub.dto.response.PartnerCalendarDayDto;
import com.bleep.learnhub.dto.response.PartnerCalendarSessionDto;
import com.bleep.learnhub.entity.Course;
import com.bleep.learnhub.entity.Batch;
import com.bleep.learnhub.entity.Session;
import com.bleep.learnhub.entity.Partner;
import com.bleep.learnhub.entity.PartnerAccessRequest;
import com.bleep.learnhub.entity.enums.AccessRequestStatus;
import com.bleep.learnhub.exception.BusinessException;
import com.bleep.learnhub.exception.ResourceNotFoundException;
import com.bleep.learnhub.repository.CourseRepository;
import com.bleep.learnhub.repository.BatchRepository;
import com.bleep.learnhub.repository.SessionRepository;
import com.bleep.learnhub.repository.PartnerRepository;
import com.bleep.learnhub.repository.PartnerAccessRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PartnerAccessService {

    private final PartnerRepository partnerRepository;
    private final PartnerAccessRequestRepository accessRequestRepository;
    private final CourseRepository courseRepository;
    private final BatchRepository batchRepository;
    private final SessionRepository sessionRepository;

    private Partner validateAndGetPartner(UUID partnerId, String username) {
        Partner partner = partnerRepository.findByUserUsername(username)
                .orElseThrow(() -> new BusinessException("Partner profile not found for user: " + username));
        if (partnerId != null && !partner.getId().equals(partnerId)) {
            throw new BusinessException("Access denied: You can only access your own partner data");
        }
        return partner;
    }

    @Transactional(readOnly = true)
    public List<CourseDataDto> getCourses(UUID partnerId, String username) {
        Partner loggedPartner = validateAndGetPartner(partnerId, username);
        
        // Fetch all requests for this partner to determine access status
        List<PartnerAccessRequest> requests = accessRequestRepository.findByPartnerId(loggedPartner.getId());

        if (partnerId == null) {
            // Return ALL courses (for Store) with status mapped
            return courseRepository.findAll().stream()
                    .map(course -> mapToCourseDataDto(course, requests))
                    .collect(Collectors.toList());
        } else {
            // Return ONLY courses that have a request (for My Courses)
            List<UUID> courseIds = requests.stream()
                    .map(PartnerAccessRequest::getCourseId)
                    .distinct()
                    .collect(Collectors.toList());
            if (courseIds.isEmpty()) {
                return List.of();
            }
            return courseRepository.findAllById(courseIds).stream()
                    .map(course -> mapToCourseDataDto(course, requests))
                    .collect(Collectors.toList());
        }
    }

    @Transactional(readOnly = true)
    public List<BatchDataDto> getBatches(UUID courseId, UUID partnerId, String username) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));
        String courseName = course.getTitle();

        Partner loggedPartner = validateAndGetPartner(partnerId, username);

        List<PartnerAccessRequest> requests = accessRequestRepository.findByPartnerId(loggedPartner.getId()).stream()
                .filter(r -> r.getCourseId().equals(courseId))
                .collect(Collectors.toList());

        if (partnerId == null) {
            return batchRepository.findByCourseId(courseId).stream()
                    .map(batch -> mapToBatchDataDto(batch, courseName, requests))
                    .collect(Collectors.toList());
        } else {
            if (requests.isEmpty()) {
                return List.of();
            }

            boolean hasFullCourseAccess = requests.stream()
                    .anyMatch(r -> r.getBatchId() == null);

            List<Batch> batches;
            if (hasFullCourseAccess) {
                batches = batchRepository.findByCourseId(courseId);
            } else {
                List<UUID> batchIds = requests.stream()
                        .map(PartnerAccessRequest::getBatchId)
                        .filter(Objects::nonNull)
                        .distinct()
                        .collect(Collectors.toList());
                if (batchIds.isEmpty()) {
                    return List.of();
                }
                batches = batchRepository.findAllById(batchIds).stream()
                        .filter(batch -> batch.getCourseId().equals(courseId))
                        .collect(Collectors.toList());
            }

            return batches.stream()
                    .map(batch -> mapToBatchDataDto(batch, courseName, requests))
                    .collect(Collectors.toList());
        }
    }

    @Transactional(readOnly = true)
    public List<PartnerAccessSessionDto> getSessions(UUID courseId, UUID batchId, UUID partnerId, String username) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));
        Batch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("Batch not found with id: " + batchId));

        if (!batch.getCourseId().equals(courseId)) {
            throw new BusinessException("Batch does not belong to the specified course");
        }

        Partner loggedPartner = validateAndGetPartner(partnerId, username);

        List<Session> sessions = sessionRepository.findByBatchIdOrderBySequenceOrderAsc(batchId);
        List<PartnerAccessRequest> requests = accessRequestRepository.findByPartnerId(loggedPartner.getId()).stream()
                .filter(r -> r.getCourseId().equals(courseId) && (r.getBatchId() == null || r.getBatchId().equals(batchId)))
                .collect(Collectors.toList());

        if (partnerId == null) {
            // Send sessions without links (Store view)
            return sessions.stream()
                    .map(s -> mapToPartnerAccessSessionDto(s, course.getTitle(), batch.getTitle(), false, requests))
                    .collect(Collectors.toList());
        } else {
            if (requests.isEmpty()) {
                // Partner has not requested access to this course or batch
                return List.of();
            }

            // Determine status
            boolean isApproved = requests.stream().anyMatch(r -> r.getStatus() == AccessRequestStatus.APPROVED);

            return sessions.stream()
                    .map(s -> mapToPartnerAccessSessionDto(s, course.getTitle(), batch.getTitle(), isApproved, requests))
                    .collect(Collectors.toList());
        }
    }

    @Transactional(readOnly = true)
    public List<PartnerCalendarDayDto> getSchedule(LocalDate fromDate, LocalDate toDate, UUID partnerId, String username) {
        if (fromDate == null || toDate == null || partnerId == null) {
            throw new BusinessException("All parameters (fromDate, toDate, partnerId) are required");
        }
        if (fromDate.isAfter(toDate)) {
            throw new BusinessException("fromDate must be before or equal to toDate");
        }

        validateAndGetPartner(partnerId, username);

        // Find all requests for this partner
        List<PartnerAccessRequest> requests = accessRequestRepository.findByPartnerId(partnerId);
        if (requests.isEmpty()) {
            return buildEmptyCalendar(fromDate, toDate);
        }

        // Identify requested batches
        List<UUID> courseIdsForFullAccess = requests.stream()
                .filter(r -> r.getBatchId() == null)
                .map(PartnerAccessRequest::getCourseId)
                .distinct()
                .collect(Collectors.toList());

        List<UUID> specificBatchIds = requests.stream()
                .map(PartnerAccessRequest::getBatchId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        // Find all batches from both course-level (full course access) and specific batch-level requests
        List<Batch> batches = new ArrayList<>();
        if (!courseIdsForFullAccess.isEmpty()) {
            for (UUID courseId : courseIdsForFullAccess) {
                batches.addAll(batchRepository.findByCourseId(courseId));
            }
        }
        if (!specificBatchIds.isEmpty()) {
            batches.addAll(batchRepository.findAllById(specificBatchIds));
        }

        List<UUID> requestedBatchIds = batches.stream()
                .map(Batch::getId)
                .distinct()
                .collect(Collectors.toList());

        if (requestedBatchIds.isEmpty()) {
            return buildEmptyCalendar(fromDate, toDate);
        }

        // Fetch all sessions for these batch IDs scheduled within fromDate and toDate
        List<Session> sessions = sessionRepository.findByBatchIdsAndScheduledDateBetween(requestedBatchIds, fromDate, toDate);

        // Group sessions by scheduledDate
        Map<LocalDate, List<Session>> sessionsByDate = sessions.stream()
                .filter(s -> s.getScheduledDate() != null)
                .collect(Collectors.groupingBy(Session::getScheduledDate));

        // Load courses and batches for mapping
        Map<UUID, String> courseNames = new HashMap<>();
        Map<UUID, String> batchNames = new HashMap<>();
        for (Batch b : batches) {
            batchNames.put(b.getId(), b.getTitle());
            courseNames.computeIfAbsent(b.getCourseId(), cid -> 
                courseRepository.findById(cid).map(Course::getTitle).orElse(null)
            );
        }

        List<PartnerCalendarDayDto> calendar = new ArrayList<>();
        LocalDate current = fromDate;
        while (!current.isAfter(toDate)) {
            List<Session> dateSessions = sessionsByDate.getOrDefault(current, List.of());
            List<PartnerCalendarSessionDto> mappedSessions = dateSessions.stream()
                    .map(s -> {
                        String cName = courseNames.get(s.getCourseId());
                        if (cName == null) {
                            cName = courseRepository.findById(s.getCourseId()).map(Course::getTitle).orElse(null);
                            if (cName != null) {
                                courseNames.put(s.getCourseId(), cName);
                            }
                        }
                        String bName = batchNames.get(s.getBatchId());
                        if (bName == null) {
                            bName = batchRepository.findById(s.getBatchId()).map(Batch::getTitle).orElse(null);
                            if (bName != null) {
                                batchNames.put(s.getBatchId(), bName);
                            }
                        }
                        return PartnerCalendarSessionDto.builder()
                                .sessionId(s.getId())
                                .courseId(s.getCourseId())
                                .courseName(cName)
                                .batchId(s.getBatchId())
                                .batchName(bName)
                                .title(s.getTitle())
                                .subtitle(s.getSubtitle())
                                .description(s.getDescription())
                                .sequenceOrder(s.getSequenceOrder())
                                .scheduledDate(s.getScheduledDate() != null ? s.getScheduledDate().toString() : null)
                                .scheduledTime(s.getScheduledTime() != null ? s.getScheduledTime().toString() : null)
                                .build();
                    })
                    .collect(Collectors.toList());

            calendar.add(PartnerCalendarDayDto.builder()
                    .date(current)
                    .sessions(mappedSessions)
                    .build());

            current = current.plusDays(1);
        }

        return calendar;
    }

    private List<PartnerCalendarDayDto> buildEmptyCalendar(LocalDate fromDate, LocalDate toDate) {
        List<PartnerCalendarDayDto> calendar = new ArrayList<>();
        LocalDate current = fromDate;
        while (!current.isAfter(toDate)) {
            calendar.add(PartnerCalendarDayDto.builder()
                    .date(current)
                    .sessions(List.of())
                    .build());
            current = current.plusDays(1);
        }
        return calendar;
    }

    private CourseDataDto mapToCourseDataDto(Course course, List<PartnerAccessRequest> requests) {
        Optional<PartnerAccessRequest> matchingRequest = requests.stream()
                .filter(r -> r.getCourseId().equals(course.getId()))
                .findFirst(); // Since we only care if they requested it

        return CourseDataDto.builder()
                .id(course.getId())
                .title(course.getTitle())
                .subtitle(course.getSubtitle())
                .description(course.getDescription())
                .category(course.getCategory())
                .createdAt(course.getCreatedAt() != null ? course.getCreatedAt().toString() : null)
                .updatedAt(course.getUpdatedAt() != null ? course.getUpdatedAt().toString() : null)
                .hasRequestedAccess(matchingRequest.isPresent())
                .accessStatus(matchingRequest.map(r -> r.getStatus().name()).orElse(null))
                .build();
    }

    private BatchDataDto mapToBatchDataDto(Batch batch, String courseName, List<PartnerAccessRequest> requests) {
        // A partner has access to a batch if they requested the specific batch, or the entire course (batchId == null)
        Optional<PartnerAccessRequest> matchingRequest = requests.stream()
                .filter(r -> r.getBatchId() == null || r.getBatchId().equals(batch.getId()))
                .findFirst();

        return BatchDataDto.builder()
                .id(batch.getId())
                .courseId(batch.getCourseId())
                .courseName(courseName)
                .title(batch.getTitle())
                .subtitle(batch.getSubtitle())
                .description(batch.getDescription())
                .startingDate(batch.getStartingDate() != null ? batch.getStartingDate().toString() : null)
                .endingDate(batch.getEndingDate() != null ? batch.getEndingDate().toString() : null)
                .createdAt(batch.getCreatedAt() != null ? batch.getCreatedAt().toString() : null)
                .updatedAt(batch.getUpdatedAt() != null ? batch.getUpdatedAt().toString() : null)
                .hasRequestedAccess(matchingRequest.isPresent())
                .accessStatus(matchingRequest.map(r -> r.getStatus().name()).orElse(null))
                .build();
    }

    private PartnerAccessSessionDto mapToPartnerAccessSessionDto(Session session, String courseName, String batchName, boolean includeLinks, List<PartnerAccessRequest> requests) {
        Optional<PartnerAccessRequest> matchingRequest = requests.stream()
                .filter(r -> r.getBatchId() == null || r.getBatchId().equals(session.getBatchId()))
                .findFirst();

        return PartnerAccessSessionDto.builder()
                .id(session.getId())
                .courseId(session.getCourseId())
                .courseName(courseName)
                .batchId(session.getBatchId())
                .batchName(batchName)
                .title(session.getTitle())
                .subtitle(session.getSubtitle())
                .description(session.getDescription())
                .sessionType(session.getSessionType() != null ? session.getSessionType().name() : null)
                .sequenceOrder(session.getSequenceOrder())
                .scheduledDate(session.getScheduledDate() != null ? session.getScheduledDate().toString() : null)
                .scheduledTime(session.getScheduledTime() != null ? session.getScheduledTime().toString() : null)
                .liveLink(includeLinks ? session.getLiveLink() : null)
                .recordedLink(includeLinks ? session.getRecordedLink() : null)
                .resourceLink(includeLinks ? session.getResourceLink() : null)
                .hasRequestedAccess(matchingRequest.isPresent())
                .accessStatus(matchingRequest.map(r -> r.getStatus().name()).orElse(null))
                .build();
    }
}
