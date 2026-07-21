package com.bleep.learnhub.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import java.util.UUID;

import com.bleep.learnhub.entity.*;
import com.bleep.learnhub.repository.*;

@Service
@RequiredArgsConstructor
public class MasterDataService {

    private final BatchRepository batchRepository;
    private final CourseRepository courseRepository;
    private final PartnerRepository partnerRepository;
    private final PartnerAccessRequestRepository partnerAccessRequestRepository;
    private final SessionRepository sessionRepository;
    private final StudentRepository studentRepository;
    private final StudentComplaintRepository studentComplaintRepository;
    private final StudentEnrollmentRepository studentEnrollmentRepository;
    private final UserRepository userRepository;
    private final UserSessionRepository userSessionRepository;
    private final VendorRepository vendorRepository;
    private final StudentSessionLogRepository studentSessionLogRepository;

    private <T> Specification<T> searchSpec(String search) {
        return (root, query, criteriaBuilder) -> {
            if (search == null || search.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            String pattern = "%" + search.toLowerCase() + "%";
            var predicates = root.getModel().getAttributes().stream()
                .filter(a -> a.getJavaType().equals(String.class))
                .map(a -> criteriaBuilder.like(criteriaBuilder.lower(root.get(a.getName())), pattern))
                .toArray(jakarta.persistence.criteria.Predicate[]::new);
            
            return predicates.length > 0 ? criteriaBuilder.or(predicates) : criteriaBuilder.conjunction();
        };
    }

    // Batch
    public Page<Batch> getAllBatches(Pageable pageable, String search) { return batchRepository.findAll(searchSpec(search), pageable); }
    public void deleteBatch(UUID id) { batchRepository.deleteById(id); }
    public void deleteAllBatches() { batchRepository.deleteAll(); }

    // Course
    public Page<Course> getAllCourses(Pageable pageable, String search) { return courseRepository.findAll(searchSpec(search), pageable); }
    public void deleteCourse(UUID id) { courseRepository.deleteById(id); }
    public void deleteAllCourses() { courseRepository.deleteAll(); }

    // Partner
    public Page<Partner> getAllPartners(Pageable pageable, String search) { return partnerRepository.findAll(searchSpec(search), pageable); }
    public void deletePartner(UUID id) { partnerRepository.deleteById(id); }
    public void deleteAllPartners() { partnerRepository.deleteAll(); }

    // PartnerAccessRequest
    public Page<PartnerAccessRequest> getAllPartnerAccessRequests(Pageable pageable, String search) { return partnerAccessRequestRepository.findAll(searchSpec(search), pageable); }
    public void deletePartnerAccessRequest(UUID id) { partnerAccessRequestRepository.deleteById(id); }
    public void deleteAllPartnerAccessRequests() { partnerAccessRequestRepository.deleteAll(); }

    // Session
    public Page<Session> getAllSessions(Pageable pageable, String search) { return sessionRepository.findAll(searchSpec(search), pageable); }
    public void deleteSession(UUID id) { sessionRepository.deleteById(id); }
    public void deleteAllSessions() { sessionRepository.deleteAll(); }

    // Student
    public Page<Student> getAllStudents(Pageable pageable, String search) { return studentRepository.findAll(searchSpec(search), pageable); }
    public void deleteStudent(UUID id) { studentRepository.deleteById(id); }
    public void deleteAllStudents() { studentRepository.deleteAll(); }

    // StudentComplaint
    public Page<StudentComplaint> getAllStudentComplaints(Pageable pageable, String search) { return studentComplaintRepository.findAll(searchSpec(search), pageable); }
    public void deleteStudentComplaint(UUID id) { studentComplaintRepository.deleteById(id); }
    public void deleteAllStudentComplaints() { studentComplaintRepository.deleteAll(); }

    // StudentEnrollment
    public Page<StudentEnrollment> getAllStudentEnrollments(Pageable pageable, String search) { return studentEnrollmentRepository.findAll(searchSpec(search), pageable); }
    public void deleteStudentEnrollment(UUID id) { studentEnrollmentRepository.deleteById(id); }
    public void deleteAllStudentEnrollments() { studentEnrollmentRepository.deleteAll(); }

    // User
    public Page<User> getAllUsers(Pageable pageable, String search) { return userRepository.findAll(searchSpec(search), pageable); }
    public void deleteUser(UUID id) { userRepository.deleteById(id); }
    public void deleteAllUsers() { userRepository.deleteAll(); }

    // UserSession
    public Page<UserSession> getAllUserSessions(Pageable pageable, String search) { return userSessionRepository.findAll(searchSpec(search), pageable); }
    public void deleteUserSession(UUID id) { userSessionRepository.deleteById(id); }
    public void deleteAllUserSessions() { userSessionRepository.deleteAll(); }

    // Vendor
    public Page<Vendor> getAllVendors(Pageable pageable, String search) { return vendorRepository.findAll(searchSpec(search), pageable); }
    public void deleteVendor(UUID id) { vendorRepository.deleteById(id); }
    public void deleteAllVendors() { vendorRepository.deleteAll(); }

    // StudentSessionLog
    public Page<StudentSessionLog> getAllStudentSessionLogs(Pageable pageable, String search) { return studentSessionLogRepository.findAll(searchSpec(search), pageable); }
    public void deleteStudentSessionLog(UUID id) { studentSessionLogRepository.deleteById(id); }
    public void deleteAllStudentSessionLogs() { studentSessionLogRepository.deleteAll(); }
}
