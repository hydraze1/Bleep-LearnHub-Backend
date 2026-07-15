package com.bleep.learnhub.service;

import com.bleep.learnhub.dto.StudentDataSyncRequest;
import com.bleep.learnhub.dto.response.StudentDataSyncResponseDto;
import com.bleep.learnhub.entity.Student;
import com.bleep.learnhub.entity.StudentEnrollment;
import com.bleep.learnhub.entity.StudentSessionLog;
import com.bleep.learnhub.repository.StudentEnrollmentRepository;
import com.bleep.learnhub.repository.StudentRepository;
import com.bleep.learnhub.repository.StudentSessionLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StudentDataSyncService {

    private final StudentRepository studentRepository;
    private final StudentEnrollmentRepository studentEnrollmentRepository;
    private final StudentSessionLogRepository studentSessionLogRepository;

    @Transactional
    public StudentDataSyncResponseDto syncStudentData(StudentDataSyncRequest request) {

        // ── 1. Student ──────────────────────────────────────────────
        Student student;
        if (request.getStudentId() != null) {
            // studentId sent → use existing, don't save
            student = studentRepository.findById(request.getStudentId())
                    .orElseThrow(() -> new RuntimeException("Student not found with id: " + request.getStudentId()));
        } else {
            // studentId not sent → check if student with this email already exists
            java.util.Optional<Student> existingStudent = studentRepository.findByEmail(request.getEmail());
            if (existingStudent.isPresent()) {
                student = existingStudent.get();
            } else {
                // create new student
                student = Student.builder()
                        .partnerId(request.getPartnerId())
                        .firstName(request.getFirstName())
                        .lastName(request.getLastName())
                        .email(request.getEmail())
                        .phoneNumber(request.getPhoneNumber())
                        .college(request.getCollege())
                        .branch(request.getBranch())
                        .academicYear(request.getAcademicYear())
                        .build();
                student = studentRepository.save(student);
            }
        }

        UUID studentId = student.getId();

        // ── 2. Enrollment ───────────────────────────────────────────
        StudentEnrollment enrollment;
        if (request.getEnrollmentId() != null) {
            // enrollmentId sent → check if courseId and batchId match
            enrollment = studentEnrollmentRepository.findById(request.getEnrollmentId())
                    .orElseThrow(() -> new RuntimeException("Enrollment not found with id: " + request.getEnrollmentId()));

            boolean courseMatches = enrollment.getCourseId().equals(request.getCourseId());
            boolean batchMatches = enrollment.getBatchId().equals(request.getEnrollmentBatchId());

            if (!courseMatches || !batchMatches) {
                // Course or batch doesn't match → save new enrollment
                enrollment = StudentEnrollment.builder()
                        .studentId(studentId)
                        .courseId(request.getCourseId())
                        .batchId(request.getEnrollmentBatchId())
                        .courseName(request.getEnrollmentCourseName())
                        .batchName(request.getEnrollmentBatchName())
                        .status(request.getEnrollmentStatus())
                        .build();
                enrollment = studentEnrollmentRepository.save(enrollment);
            }
            // If both match → skip saving, use existing enrollment
        } else {
            // enrollmentId not sent → check if enrollment already exists for this student and course and batch
            java.util.Optional<StudentEnrollment> existingEnrollment = studentEnrollmentRepository.findByStudentIdAndCourseIdAndBatchId(studentId, request.getCourseId(), request.getEnrollmentBatchId());
            if (existingEnrollment.isPresent()) {
                enrollment = existingEnrollment.get();
            } else {
                // save new enrollment
                enrollment = StudentEnrollment.builder()
                        .studentId(studentId)
                        .courseId(request.getCourseId())
                        .batchId(request.getEnrollmentBatchId())
                        .courseName(request.getEnrollmentCourseName())
                        .batchName(request.getEnrollmentBatchName())
                        .status(request.getEnrollmentStatus())
                        .build();
                enrollment = studentEnrollmentRepository.save(enrollment);
            }
        }

        // ── 3. Session Log ──────────────────────────────────────────
        StudentSessionLog sessionLog = null;
        if (request.getSessionId() != null) {
            // Check if same session log already exists for this student
            java.util.Optional<StudentSessionLog> existingSessionLog = studentSessionLogRepository.findByStudentIdAndSessionId(studentId, request.getSessionId());
            if (existingSessionLog.isPresent()) {
                sessionLog = existingSessionLog.get();
            }
        }
        
        if (sessionLog == null) {
            // New session → save
            LocalDateTime now = LocalDateTime.now();
            sessionLog = StudentSessionLog.builder()
                    .studentId(studentId)
                    .sessionId(request.getSessionId())
                    .sessionType(request.getSessionType())
                    .courseId(request.getLogCourseId())
                    .courseName(request.getLogCourseName())
                    .batchId(request.getLogBatchId())
                    .batchName(request.getLogBatchName())
                    .entryTime(now)
                    .completionTime(now)
                    .timeSpentSec(0)
                    .build();
            sessionLog = studentSessionLogRepository.save(sessionLog);
        }

        // ── 4. Build response ───────────────────────────────────────
        return StudentDataSyncResponseDto.builder()
                .student(student)
                .enrollment(enrollment)
                .sessionLog(sessionLog)
                .build();
    }
}
