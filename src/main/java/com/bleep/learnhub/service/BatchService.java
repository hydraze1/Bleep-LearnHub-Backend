package com.bleep.learnhub.service;

import com.bleep.learnhub.dto.request.BatchCreateDto;
import com.bleep.learnhub.dto.request.BatchUpdateDto;
import com.bleep.learnhub.dto.response.BatchDataDto;
import com.bleep.learnhub.dto.response.BatchProfileResponseDto;
import com.bleep.learnhub.entity.Batch;
import com.bleep.learnhub.exception.BusinessException;
import com.bleep.learnhub.exception.ResourceNotFoundException;
import com.bleep.learnhub.repository.BatchRepository;
import com.bleep.learnhub.repository.CourseRepository;
import com.bleep.learnhub.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BatchService {

    private final BatchRepository batchRepository;
    private final CourseRepository courseRepository;
    private final SessionRepository sessionRepository;

    public BatchProfileResponseDto createBatch(BatchCreateDto dto) {
        if (!courseRepository.existsById(dto.getCourseId())) {
            throw new ResourceNotFoundException("Course not found with id: " + dto.getCourseId());
        }

        Batch batch = Batch.builder()
                .courseId(dto.getCourseId())
                .title(dto.getTitle())
                .subtitle(dto.getSubtitle())
                .description(dto.getDescription())
                .scheduledDate(dto.getScheduledDate())
                .scheduledTime(dto.getScheduledTime())
                .build();

        batch = batchRepository.save(batch);
        return mapToProfileDto(batch);
    }

    public void updateBatch(UUID id, BatchUpdateDto dto) {
        Batch batch = getBatchEntity(id);

        batch.setTitle(dto.getTitle());
        batch.setSubtitle(dto.getSubtitle());
        batch.setDescription(dto.getDescription());
        batch.setScheduledDate(dto.getScheduledDate());
        batch.setScheduledTime(dto.getScheduledTime());

        batchRepository.save(batch);
    }

    public void deleteBatch(UUID id) {
        Batch batch = getBatchEntity(id);

        long sessionCount = sessionRepository.countByBatchId(id);
        if (sessionCount > 0) {
            throw new BusinessException("Cannot delete batch with existing sessions. Delete sessions first.");
        }

        batchRepository.delete(batch);
    }

    public List<BatchDataDto> getBatchesByCourseId(UUID courseId) {
        return batchRepository.findByCourseId(courseId).stream()
                .map(this::mapToDataDto)
                .collect(Collectors.toList());
    }

    public BatchProfileResponseDto getBatchById(UUID id) {
        return mapToProfileDto(getBatchEntity(id));
    }

    private Batch getBatchEntity(UUID id) {
        return batchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Batch not found with id: " + id));
    }

    private BatchDataDto mapToDataDto(Batch batch) {
        return BatchDataDto.builder()
                .id(batch.getId())
                .courseId(batch.getCourseId())
                .title(batch.getTitle())
                .subtitle(batch.getSubtitle())
                .description(batch.getDescription())
                .scheduledDate(batch.getScheduledDate() != null ? batch.getScheduledDate().toString() : null)
                .scheduledTime(batch.getScheduledTime() != null ? batch.getScheduledTime().toString() : null)
                .createdAt(batch.getCreatedAt() != null ? batch.getCreatedAt().toString() : null)
                .updatedAt(batch.getUpdatedAt() != null ? batch.getUpdatedAt().toString() : null)
                .build();
    }

    private BatchProfileResponseDto mapToProfileDto(Batch batch) {
        return BatchProfileResponseDto.builder()
                .id(batch.getId())
                .courseId(batch.getCourseId())
                .title(batch.getTitle())
                .subtitle(batch.getSubtitle())
                .description(batch.getDescription())
                .scheduledDate(batch.getScheduledDate() != null ? batch.getScheduledDate().toString() : null)
                .scheduledTime(batch.getScheduledTime() != null ? batch.getScheduledTime().toString() : null)
                .createdAt(batch.getCreatedAt() != null ? batch.getCreatedAt().toString() : null)
                .updatedAt(batch.getUpdatedAt() != null ? batch.getUpdatedAt().toString() : null)
                .build();
    }
}
