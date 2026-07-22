package com.bleep.learnhub.service;

import com.bleep.learnhub.dto.request.ComplaintCreateDto;
import com.bleep.learnhub.dto.request.ComplaintUpdateDto;
import com.bleep.learnhub.dto.response.ComplaintResponseDto;
import com.bleep.learnhub.entity.Partner;
import com.bleep.learnhub.entity.StudentComplaint;
import com.bleep.learnhub.entity.Vendor;
import com.bleep.learnhub.entity.enums.ComplaintStatus;
import com.bleep.learnhub.exception.ResourceNotFoundException;
import com.bleep.learnhub.repository.PartnerRepository;
import com.bleep.learnhub.repository.StudentComplaintRepository;
import com.bleep.learnhub.repository.VendorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentComplaintService {

    private final StudentComplaintRepository complaintRepository;
    private final PartnerRepository partnerRepository;
    private final VendorRepository vendorRepository;

    public void createComplaint(ComplaintCreateDto dto) {
        // Resolve names once on creation so we never need extra DB queries on reads
        String partnerName = partnerRepository.findById(dto.getPartnerId())
                .map(Partner::getCompanyName).orElse(null);
        String vendorName = vendorRepository.findById(dto.getVendorId())
                .map(Vendor::getCompanyName).orElse(null);

        StudentComplaint complaint = StudentComplaint.builder()
                .studentId(dto.getStudentId())
                .partnerId(dto.getPartnerId())
                .vendorId(dto.getVendorId())
                .courseId(dto.getCourseId())
                .batchId(dto.getBatchId())
                .studentName(dto.getStudentName())
                .email(dto.getEmail())
                .phoneNumber(dto.getPhoneNumber())
                .college(dto.getCollege())
                .branch(dto.getBranch())
                .academicYear(dto.getAcademicYear())
                .courseName(dto.getCourseName())
                .batchName(dto.getBatchName())
                .partnerName(partnerName)
                .vendorName(vendorName)
                .complaintTitle(dto.getComplaintTitle())
                .complaintText(dto.getComplaintText())
                .status(ComplaintStatus.PENDING)
                .build();

        complaintRepository.save(complaint);
    }

    public void updateComplaint(UUID id, ComplaintUpdateDto dto, boolean isVendor, boolean isPartner) {
        StudentComplaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with id: " + id));

        if (dto.getStatus() != null) {
            if (dto.getStatus() == ComplaintStatus.RESOLVED && complaint.getStatus() != ComplaintStatus.RESOLVED) {
                if (isVendor) {
                    complaint.setIsResolvedByVendor(true);
                }
                if (isPartner) {
                    complaint.setIsResolvedByPartner(true);
                }
            }
            complaint.setStatus(dto.getStatus());
        }

        if (isVendor && dto.getVendorRemark() != null) {
            complaint.setVendorRemark(dto.getVendorRemark());
        }
        
        if (isPartner && dto.getPartnerRemark() != null) {
            complaint.setPartnerRemark(dto.getPartnerRemark());
        }

        complaintRepository.save(complaint);
    }

    public void deleteComplaint(UUID id) {
        StudentComplaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with id: " + id));
        complaintRepository.delete(complaint);
    }

    public List<ComplaintResponseDto> getComplaintsByVendorId(UUID vendorId) {
        return complaintRepository.findByVendorId(vendorId).stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    public List<ComplaintResponseDto> getComplaintsByPartnerId(UUID partnerId) {
        return complaintRepository.findByPartnerId(partnerId).stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    public ComplaintResponseDto getComplaintById(UUID id) {
        StudentComplaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with id: " + id));
        return mapToResponseDto(complaint);
    }

    private ComplaintResponseDto mapToResponseDto(StudentComplaint complaint) {
        return ComplaintResponseDto.builder()
                .id(complaint.getId())
                .studentId(complaint.getStudentId())
                .partnerId(complaint.getPartnerId())
                .vendorId(complaint.getVendorId())
                .courseId(complaint.getCourseId())
                .batchId(complaint.getBatchId())
                .studentName(complaint.getStudentName())
                .email(complaint.getEmail())
                .phoneNumber(complaint.getPhoneNumber())
                .college(complaint.getCollege())
                .branch(complaint.getBranch())
                .academicYear(complaint.getAcademicYear())
                .courseName(complaint.getCourseName())
                .batchName(complaint.getBatchName())
                .complaintTitle(complaint.getComplaintTitle())
                .complaintText(complaint.getComplaintText())
                .status(complaint.getStatus().name())
                .vendorRemark(complaint.getVendorRemark())
                .partnerRemark(complaint.getPartnerRemark())
                .partnerName(complaint.getPartnerName())
                .vendorName(complaint.getVendorName())
                .isResolvedByVendor(complaint.getIsResolvedByVendor())
                .isResolvedByPartner(complaint.getIsResolvedByPartner())
                .createdAt(complaint.getCreatedAt() != null ? complaint.getCreatedAt().toString() : null)
                .updatedAt(complaint.getUpdatedAt() != null ? complaint.getUpdatedAt().toString() : null)
                .build();
    }
}
