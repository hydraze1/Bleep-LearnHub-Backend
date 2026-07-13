package com.bleep.learnhub.service;

import com.bleep.learnhub.dto.request.AccessRequestDto;
import com.bleep.learnhub.dto.request.AccessStatusUpdateDto;
import com.bleep.learnhub.dto.response.AccessRequestResponseDto;
import com.bleep.learnhub.entity.Batch;
import com.bleep.learnhub.entity.Course;
import com.bleep.learnhub.entity.Partner;
import com.bleep.learnhub.entity.PartnerAccessRequest;
import com.bleep.learnhub.entity.enums.AccessRequestStatus;
import com.bleep.learnhub.exception.ResourceNotFoundException;
import com.bleep.learnhub.repository.BatchRepository;
import com.bleep.learnhub.repository.CourseRepository;
import com.bleep.learnhub.repository.PartnerAccessRequestRepository;
import com.bleep.learnhub.repository.PartnerRepository;
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
    private final CourseRepository courseRepository;
    private final BatchRepository batchRepository;

    public void createAccessRequest(AccessRequestDto dto, String partnerUsername) {
        Partner partner = partnerRepository.findByUserUsername(partnerUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Partner profile not found for user: " + partnerUsername));

        Course course = courseRepository.findById(dto.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + dto.getCourseId()));

        String batchName = null;
        if (dto.getBatchId() != null) {
            Batch batch = batchRepository.findById(dto.getBatchId())
                    .orElseThrow(() -> new ResourceNotFoundException("Batch not found with id: " + dto.getBatchId()));
            batchName = batch.getTitle();
        }

        PartnerAccessRequest request = PartnerAccessRequest.builder()
                .partnerId(partner.getId())
                .vendorId(dto.getVendorId())
                .courseId(dto.getCourseId())
                .batchId(dto.getBatchId())
                .partnerName(partner.getCompanyName())
                .courseName(course.getTitle())
                .batchName(batchName)
                .status(AccessRequestStatus.PENDING)
                .requestNote(dto.getRequestNote())
                .hasBatchAccess(dto.getHasBatchAccess())
                .build();

        accessRequestRepository.save(request);
    }

    public void updateAccessStatus(UUID id, AccessStatusUpdateDto dto) {
        PartnerAccessRequest request = accessRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Access request not found with id: " + id));

        request.setStatus(dto.getStatus());
        request.setResponseNote(dto.getResponseNote());
        
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
                .responseNote(request.getResponseNote())
                .requestedAt(request.getRequestedAt() != null ? request.getRequestedAt().toString() : null)
                .resolvedAt(request.getResolvedAt() != null ? request.getResolvedAt().toString() : null)
                .build();
    }
}
