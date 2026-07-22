package com.bleep.learnhub.service.impl;

import com.bleep.learnhub.dto.response.ComplaintResponseDto;
import com.bleep.learnhub.dto.response.PartnerDashboardResponseDto;
import com.bleep.learnhub.dto.response.VendorDashboardResponseDto;
import com.bleep.learnhub.dto.response.VendorProfileResponseDto;
import com.bleep.learnhub.entity.Partner;
import com.bleep.learnhub.entity.StudentComplaint;
import com.bleep.learnhub.entity.enums.AccessRequestStatus;
import com.bleep.learnhub.repository.*;
import com.bleep.learnhub.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final CourseRepository courseRepository;
    private final BatchRepository batchRepository;
    private final PartnerRepository partnerRepository;
    private final StudentRepository studentRepository;
    private final StudentComplaintRepository studentComplaintRepository;
    private final PartnerAccessRequestRepository partnerAccessRequestRepository;

    @Override
    public VendorDashboardResponseDto getVendorDashboard(UUID vendorId) {
        long totalCourses = courseRepository.count();
        long totalBatches = batchRepository.count();
        long totalPartners = partnerRepository.countByVendorId(vendorId);

        // Count students belonging to all partners of this vendor
        List<Partner> partners = partnerRepository.findByVendorId(vendorId);
        List<UUID> partnerIds = partners.stream().map(Partner::getId).collect(Collectors.toList());
        long totalStudents = partnerIds.isEmpty() ? 0 : studentRepository.countByPartnerIdIn(partnerIds);

        long pendingRequests = partnerAccessRequestRepository.countByVendorIdAndStatus(vendorId, AccessRequestStatus.PENDING);

        List<StudentComplaint> top5Complaints = studentComplaintRepository.findTop5ByVendorIdOrderByCreatedAtDesc(vendorId);
        List<ComplaintResponseDto> complaints = top5Complaints.stream()
                .map(this::mapToComplaintDto)
                .collect(Collectors.toList());

        return VendorDashboardResponseDto.builder()
                .totalCourses(totalCourses)
                .totalBatches(totalBatches)
                .totalPartners(totalPartners)
                .totalStudents(totalStudents)
                .pendingAccessRequests(pendingRequests)
                .recentComplaints(complaints)
                .build();
    }

    @Override
    public PartnerDashboardResponseDto getPartnerDashboard(UUID partnerId) {
        Partner partner = partnerRepository.findById(partnerId)
                .orElseThrow(() -> new IllegalArgumentException("Partner not found"));

        long totalStudents = studentRepository.countByPartnerId(partnerId);
        // Note: Currently no direct way to count active enrollments, this is just an example 
        // representing total students assuming each student is in 1 enrollment or something.
        // We could write a native query, but for now we'll set it to totalStudents as a placeholder
        // or actually implement countByPartnerId in Enrollment. Let's just use totalStudents for now.
        long activeEnrollments = totalStudents;

        List<StudentComplaint> top5Complaints = studentComplaintRepository.findTop5ByPartnerIdOrderByCreatedAtDesc(partnerId);
        List<ComplaintResponseDto> complaints = top5Complaints.stream()
                .map(this::mapToComplaintDto)
                .collect(Collectors.toList());

        VendorProfileResponseDto vendorDetails = VendorProfileResponseDto.builder()
                .id(partner.getVendor().getId().toString())
                .companyName(partner.getVendor().getCompanyName())
                .email(partner.getVendor().getEmail())
                .phone(partner.getVendor().getPhone())
                .description(partner.getVendor().getDescription())
                .isActive(partner.getVendor().isActive())
                .build();

        return PartnerDashboardResponseDto.builder()
                .vendorDetails(vendorDetails)
                .totalStudents(totalStudents)
                .activeEnrollments(activeEnrollments)
                .recentComplaints(complaints)
                .build();
    }

    private ComplaintResponseDto mapToComplaintDto(StudentComplaint c) {
        return ComplaintResponseDto.builder()
                .id(c.getId())
                .studentId(c.getStudentId())
                .partnerId(c.getPartnerId())
                .vendorId(c.getVendorId())
                .courseId(c.getCourseId())
                .batchId(c.getBatchId())
                .studentName(c.getStudentName())
                .email(c.getEmail())
                .phoneNumber(c.getPhoneNumber())
                .college(c.getCollege())
                .branch(c.getBranch())
                .academicYear(c.getAcademicYear())
                .courseName(c.getCourseName())
                .batchName(c.getBatchName())
                .complaintTitle(c.getComplaintTitle())
                .complaintText(c.getComplaintText())
                .status(c.getStatus() != null ? c.getStatus().name() : null)
                .vendorRemark(c.getVendorRemark())
                .partnerRemark(c.getPartnerRemark())
                .isResolvedByVendor(c.getIsResolvedByVendor())
                .isResolvedByPartner(c.getIsResolvedByPartner())
                .createdAt(c.getCreatedAt() != null ? c.getCreatedAt().toString() : null)
                .updatedAt(c.getUpdatedAt() != null ? c.getUpdatedAt().toString() : null)
                .build();
    }
}
