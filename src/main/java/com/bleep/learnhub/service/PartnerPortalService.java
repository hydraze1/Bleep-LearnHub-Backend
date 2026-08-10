package com.bleep.learnhub.service;

import com.bleep.learnhub.dto.response.BatchDataDto;
import com.bleep.learnhub.dto.response.CourseDataDto;
import com.bleep.learnhub.dto.response.PartnerSessionResponseDto;
import com.bleep.learnhub.dto.response.PartnerStudentResponseDto;
import com.bleep.learnhub.entity.*;
import com.bleep.learnhub.entity.enums.AccessRequestStatus;
import com.bleep.learnhub.dto.response.JoinLiveClassSessionResponseDto;
import com.bleep.learnhub.exception.ResourceNotFoundException;
import com.bleep.learnhub.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PartnerPortalService {

    private final PartnerAccessRequestRepository accessRequestRepository;
    private final CourseRepository courseRepository;
    private final BatchRepository batchRepository;
    private final SessionRepository sessionRepository;
    private final StudentRepository studentRepository;
    private final StudentEnrollmentRepository studentEnrollmentRepository;
    private final StudentSessionLogRepository studentSessionLogRepository;
    private final PartnerRepository partnerRepository;

    public List<CourseDataDto> getCoursesByPartnerId(UUID partnerId) {
        List<UUID> courseIds = accessRequestRepository.findByPartnerId(partnerId).stream()
                .filter(r -> r.getStatus() == AccessRequestStatus.APPROVED)
                .map(PartnerAccessRequest::getCourseId)
                .distinct()
                .collect(Collectors.toList());

        if (courseIds.isEmpty()) {
            return List.of();
        }

        return courseRepository.findAllById(courseIds).stream()
                .map(this::mapToCourseDataDto)
                .collect(Collectors.toList());
    }

    public List<BatchDataDto> getBatchesByPartnerAndCourse(UUID partnerId, UUID courseId) {
        List<PartnerAccessRequest> requests = accessRequestRepository.findByPartnerId(partnerId).stream()
                .filter(r -> r.getStatus() == AccessRequestStatus.APPROVED && r.getCourseId().equals(courseId))
                .collect(Collectors.toList());

        if (requests.isEmpty()) {
            return List.of();
        }

        String courseName = courseRepository.findById(courseId)
                .map(Course::getTitle)
                .orElse(null);

        boolean hasFullCourseAccess = requests.stream()
                .anyMatch(r -> r.getBatchId() == null);

        List<Batch> batches;
        if (hasFullCourseAccess) {
            batches = batchRepository.findByCourseId(courseId);
        } else {
            List<UUID> batchIds = requests.stream()
                    .map(PartnerAccessRequest::getBatchId)
                    .filter(java.util.Objects::nonNull)
                    .collect(Collectors.toList());
            batches = batchRepository.findAllById(batchIds);
        }

        return batches.stream()
                .map(batch -> mapToBatchDataDto(batch, courseName))
                .collect(Collectors.toList());
    }

    public List<PartnerSessionResponseDto> getSessionsByBatchAndPartner(UUID partnerId, UUID batchId) {
        Batch batch = batchRepository.findById(batchId).orElse(null);
        String batchName = batch != null ? batch.getTitle() : null;
        String courseName = null;
        UUID courseId = batch != null ? batch.getCourseId() : null;
        if (courseId != null) {
            courseName = courseRepository.findById(courseId)
                    .map(Course::getTitle)
                    .orElse(null);
        }

        // Look up maxStudents from the partner access request (batch-level first, then course-level)
        Integer maxStudents = accessRequestRepository.findByPartnerId(partnerId).stream()
                .filter(r -> r.getStatus() == AccessRequestStatus.APPROVED)
                .filter(r -> r.getBatchId() != null && r.getBatchId().equals(batchId))
                .map(PartnerAccessRequest::getMaxStudents)
                .filter(java.util.Objects::nonNull)
                .findFirst()
                .orElseGet(() -> accessRequestRepository.findByPartnerId(partnerId).stream()
                        .filter(r -> r.getStatus() == AccessRequestStatus.APPROVED && r.getCourseId().equals(courseId) && r.getBatchId() == null)
                        .map(PartnerAccessRequest::getMaxStudents)
                        .filter(java.util.Objects::nonNull)
                        .findFirst()
                        .orElse(null));

        List<Session> sessions = sessionRepository.findByBatchIdOrderBySequenceOrderAsc(batchId);

        final String finalCourseName = courseName;
        return sessions.stream().map(session -> {
            long studentCount = studentSessionLogRepository.countDistinctStudentsBySessionIdAndPartnerId(session.getId(), partnerId);
            boolean limitExceeded = maxStudents != null && studentCount > maxStudents;
            return PartnerSessionResponseDto.builder()
                    .id(session.getId())
                    .courseId(session.getCourseId())
                    .courseName(finalCourseName)
                    .batchId(session.getBatchId())
                    .batchName(batchName)
                    .sessionType(session.getSessionType().name())
                    .title(session.getTitle())
                    .subtitle(session.getSubtitle())
                    .description(session.getDescription())
                    .liveLink(session.getLiveLink())
                    .recordedLink(session.getRecordedLink())
                    .resourceLink(session.getResourceLink())
                    .sequenceOrder(session.getSequenceOrder())
                    .scheduledDate(session.getScheduledDate() != null ? session.getScheduledDate().toString() : null)
                    .scheduledTime(session.getScheduledTime() != null ? session.getScheduledTime().toString() : null)
                    .studentCount(studentCount)
                    .maxStudents(maxStudents)
                    .limitExceeded(limitExceeded)
                    .build();
        }).collect(Collectors.toList());
    }

    public List<PartnerStudentResponseDto> getStudentsByPartner(UUID partnerId) {
        List<Student> students = studentRepository.findByPartnerId(partnerId);

        return students.stream().map(student -> {
            List<StudentEnrollment> enrollments = studentEnrollmentRepository.findByStudentId(student.getId());
            List<PartnerStudentResponseDto.EnrollmentDto> enrollmentDtos = enrollments.stream().map(e -> 
                PartnerStudentResponseDto.EnrollmentDto.builder()
                        .id(e.getId())
                        .courseId(e.getCourseId())
                        .batchId(e.getBatchId())
                        .courseName(e.getCourseName())
                        .batchName(e.getBatchName())
                        .status(e.getStatus().name())
                        .enrolledAt(e.getEnrolledAt() != null ? e.getEnrolledAt().toString() : null)
                        .completedAt(e.getCompletedAt() != null ? e.getCompletedAt().toString() : null)
                        .build()
            ).collect(Collectors.toList());

            return PartnerStudentResponseDto.builder()
                    .id(student.getId())
                    .firstName(student.getFirstName())
                    .lastName(student.getLastName())
                    .fullName(student.getFullName())
                    .email(student.getEmail())
                    .phoneNumber(student.getPhoneNumber())
                    .college(student.getCollege())
                    .branch(student.getBranch())
                    .academicYear(student.getAcademicYear())
                    .enrollments(enrollmentDtos)
                    .build();
        }).collect(Collectors.toList());
    }

    public List<PartnerSessionResponseDto> getLimitCrossedSessionsByPartner(UUID partnerId) {
        List<PartnerAccessRequest> requests = accessRequestRepository.findByPartnerId(partnerId).stream()
                .filter(r -> r.getStatus() == AccessRequestStatus.APPROVED)
                .collect(Collectors.toList());

        if (requests.isEmpty()) {
            return List.of();
        }

        List<Batch> allBatches = new java.util.ArrayList<>();
        for (PartnerAccessRequest request : requests) {
            if (request.getBatchId() != null) {
                batchRepository.findById(request.getBatchId()).ifPresent(allBatches::add);
            } else if (request.getCourseId() != null) {
                allBatches.addAll(batchRepository.findByCourseId(request.getCourseId()));
            }
        }

        return allBatches.stream()
                .distinct()
                .flatMap(batch -> getSessionsByBatchAndPartner(partnerId, batch.getId()).stream())
                .filter(PartnerSessionResponseDto::isLimitExceeded)
                .collect(Collectors.toList());
    }

    private CourseDataDto mapToCourseDataDto(Course course) {
        return CourseDataDto.builder()
                .id(course.getId())
                .title(course.getTitle())
                .subtitle(course.getSubtitle())
                .description(course.getDescription())
                .category(course.getCategory())
                .createdAt(course.getCreatedAt() != null ? course.getCreatedAt().toString() : null)
                .updatedAt(course.getUpdatedAt() != null ? course.getUpdatedAt().toString() : null)
                .build();
    }

    private BatchDataDto mapToBatchDataDto(Batch batch, String courseName) {
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
                .build();
    }

    public JoinLiveClassSessionResponseDto getOpenSessionDetails(UUID sessionId, UUID partnerId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found with ID: " + sessionId));

        String batchName = "";
        if (session.getBatchId() != null) {
            Batch batch = batchRepository.findById(session.getBatchId()).orElse(null);
            if (batch != null) {
                batchName = batch.getTitle();
            }
        }

        String courseName = "";
        if (session.getCourseId() != null) {
            courseName = courseRepository.findById(session.getCourseId())
                    .map(Course::getTitle)
                    .orElse("");
        }

        String companyName = "";
        if (partnerId != null) {
            companyName = partnerRepository.findById(partnerId)
                    .map(Partner::getCompanyName)
                    .orElse("");
        }

        return JoinLiveClassSessionResponseDto.builder()
                .session(JoinLiveClassSessionResponseDto.SessionDto.builder()
                        .id(session.getId() != null ? session.getId().toString() : "")
                        .courseId(session.getCourseId() != null ? session.getCourseId().toString() : "")
                        .courseName(courseName)
                        .batchId(session.getBatchId() != null ? session.getBatchId().toString() : "")
                        .batchName(batchName)
                        .sessionType(session.getSessionType() != null ? session.getSessionType().name() : "CLASS")
                        .title(session.getTitle() != null ? session.getTitle() : "")
                        .subtitle(session.getSubtitle() != null ? session.getSubtitle() : "")
                        .liveLink(session.getLiveLink() != null ? session.getLiveLink() : "")
                        .resourceLink(session.getResourceLink() != null ? session.getResourceLink() : "")
                        .scheduledDate(session.getScheduledDate() != null ? session.getScheduledDate().toString() : "")
                        .scheduledTime(session.getScheduledTime() != null ? session.getScheduledTime().toString() : "")
                        .build())
                .partnerDetails(JoinLiveClassSessionResponseDto.PartnerDto.builder()
                        .id(partnerId != null ? partnerId.toString() : "")
                        .companyName(companyName)
                        .build())
                .build();
    }
}
