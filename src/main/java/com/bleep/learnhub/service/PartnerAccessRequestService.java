package com.bleep.learnhub.service;

import com.bleep.learnhub.dto.request.AccessStatusUpdateDto;
import com.bleep.learnhub.dto.request.PartnerAccessRequestCreateDto;
import com.bleep.learnhub.dto.request.VendorAccessRequestCreateDto;
import com.bleep.learnhub.dto.response.AccessRequestResponseDto;
import com.bleep.learnhub.entity.Partner;
import com.bleep.learnhub.entity.Vendor;
import com.bleep.learnhub.entity.PartnerAccessRequest;
import com.bleep.learnhub.entity.enums.AccessRequestStatus;
import com.bleep.learnhub.exception.BusinessException;
import com.bleep.learnhub.exception.ResourceNotFoundException;
import com.bleep.learnhub.repository.PartnerAccessRequestRepository;
import com.bleep.learnhub.repository.PartnerRepository;
import com.bleep.learnhub.repository.VendorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PartnerAccessRequestService {

    private final PartnerAccessRequestRepository accessRequestRepository;
    private final PartnerRepository partnerRepository;
    private final VendorRepository vendorRepository;

    public void createAccessRequestByVendor(VendorAccessRequestCreateDto dto, String vendorUsername) {
        Vendor vendor = vendorRepository.findByUserUsername(vendorUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor profile not found for user: " + vendorUsername));

        // Verify that the partner exists
        if (!partnerRepository.existsById(dto.getPartnerId())) {
            throw new ResourceNotFoundException("Partner profile not found with id: " + dto.getPartnerId());
        }

        if (accessRequestRepository.existsByPartnerIdAndCourseIdAndBatchId(dto.getPartnerId(), dto.getCourseId(), dto.getBatchId())) {
            throw new BusinessException("Partner already has an access request for this batch of the course");
        }

        PartnerAccessRequest request = PartnerAccessRequest.builder()
                .partnerId(dto.getPartnerId())
                .vendorId(vendor.getId())
                .courseId(dto.getCourseId())
                .batchId(dto.getBatchId())
                .partnerName(dto.getPartnerName())
                .courseName(dto.getCourseName())
                .batchName(dto.getBatchName())
                .status(AccessRequestStatus.APPROVED)
                .responseNote(dto.getNote())
                .maxStudents(dto.getMaxStudents())
                .resolvedAt(LocalDateTime.now())
                .build();

        accessRequestRepository.save(request);
    }

    public void createAccessRequestByPartner(PartnerAccessRequestCreateDto dto, String partnerUsername) {
        Partner partner = partnerRepository.findByUserUsername(partnerUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Partner profile not found for user: " + partnerUsername));

        // The vendor is the partner's parent vendor
        UUID vendorId = partner.getVendor().getId();

        if (accessRequestRepository.existsByPartnerIdAndCourseIdAndBatchId(dto.getPartnerId(), dto.getCourseId(), dto.getBatchId())) {
            throw new BusinessException("Partner already has an access request for this batch of the course");
        }

        PartnerAccessRequest request = PartnerAccessRequest.builder()
                .partnerId(dto.getPartnerId())
                .vendorId(vendorId)
                .courseId(dto.getCourseId())
                .batchId(dto.getBatchId())
                .partnerName(dto.getPartnerName())
                .courseName(dto.getCourseName())
                .batchName(dto.getBatchName())
                .status(AccessRequestStatus.PENDING)
                .requestNote(dto.getNote())
                .maxStudents(dto.getMaxStudents())
                .build();

        accessRequestRepository.save(request);
    }

    public void updateAccessStatus(UUID id, AccessStatusUpdateDto dto) {
        PartnerAccessRequest request = accessRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Access request not found with id: " + id));

        request.setStatus(dto.getStatus());
        request.setResponseNote(dto.getResponseNote());

        if (dto.getMaxStudents() != null) {
            request.setMaxStudents(dto.getMaxStudents());
        }
        if (dto.getCourseId() != null) {
            request.setCourseId(dto.getCourseId());
        }
        if (dto.getCourseName() != null) {
            request.setCourseName(dto.getCourseName());
        }
        if (dto.getBatchId() != null) {
            request.setBatchId(dto.getBatchId());
        }
        if (dto.getBatchName() != null) {
            request.setBatchName(dto.getBatchName());
        }
        
        if (dto.getStatus() == AccessRequestStatus.APPROVED || dto.getStatus() == AccessRequestStatus.REJECTED) {
            request.setResolvedAt(LocalDateTime.now());
        }

        accessRequestRepository.save(request);
    }

    public void deleteAccessRequest(UUID id) {
        PartnerAccessRequest request = accessRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Access request not found with id: " + id));
        accessRequestRepository.delete(request);
    }

    public List<AccessRequestResponseDto> getRequestsByVendorId(UUID vendorId) {
        return accessRequestRepository.findByVendorId(vendorId).stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }
    
    public List<AccessRequestResponseDto> getRequestsByPartnerId(UUID partnerId) {
        return accessRequestRepository.findByPartnerId(partnerId).stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    private AccessRequestResponseDto mapToResponseDto(PartnerAccessRequest request) {
        return AccessRequestResponseDto.builder()
                .id(request.getId())
                .partnerId(request.getPartnerId())
                .vendorId(request.getVendorId())
                .courseId(request.getCourseId())
                .batchId(request.getBatchId())
                .partnerName(request.getPartnerName())
                .courseName(request.getCourseName())
                .batchName(request.getBatchName())
                .status(request.getStatus().name())
                .requestNote(request.getRequestNote())
                .hasBatchAccess(request.getHasBatchAccess())
                .maxStudents(request.getMaxStudents())
                .responseNote(request.getResponseNote())
                .requestedAt(request.getRequestedAt() != null ? request.getRequestedAt().toString() : null)
                .resolvedAt(request.getResolvedAt() != null ? request.getResolvedAt().toString() : null)
                .build();
    }
}
