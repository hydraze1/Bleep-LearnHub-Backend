package com.bleep.learnhub.service;

import com.bleep.learnhub.dto.request.CourseCreateDto;
import com.bleep.learnhub.dto.request.CourseUpdateDto;
import com.bleep.learnhub.dto.response.CourseDataDto;
import com.bleep.learnhub.dto.response.CourseProfileResponseDto;
import com.bleep.learnhub.entity.Course;
import com.bleep.learnhub.exception.BusinessException;
import com.bleep.learnhub.exception.ResourceNotFoundException;
import com.bleep.learnhub.repository.BatchRepository;
import com.bleep.learnhub.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final BatchRepository batchRepository;

    public CourseProfileResponseDto createCourse(CourseCreateDto dto) {
        Course course = Course.builder()
                .title(dto.getTitle())
                .subtitle(dto.getSubtitle())
                .description(dto.getDescription())
                .category(dto.getCategory())
                .build();
        
        course = courseRepository.save(course);
        return mapToProfileDto(course);
    }

    public void updateCourse(UUID id, CourseUpdateDto dto) {
        Course course = getCourseEntity(id);
        
        course.setTitle(dto.getTitle());
        course.setSubtitle(dto.getSubtitle());
        course.setDescription(dto.getDescription());
        course.setCategory(dto.getCategory());
        
        courseRepository.save(course);
    }

    public void deleteCourse(UUID id) {
        Course course = getCourseEntity(id);
        
        long batchCount = batchRepository.countByCourseId(id);
        if (batchCount > 0) {
            throw new BusinessException("Cannot delete course with existing batches. Delete batches first.", "COURSE_HAS_BATCHES");
        }
        
        courseRepository.delete(course);
    }

    public List<CourseDataDto> getAllCourses() {
        return courseRepository.findAll().stream()
                .map(this::mapToDataDto)
                .collect(Collectors.toList());
    }

    public CourseProfileResponseDto getCourseById(UUID id) {
        return mapToProfileDto(getCourseEntity(id));
    }

    private Course getCourseEntity(UUID id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));
    }

    private CourseDataDto mapToDataDto(Course course) {
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

    private CourseProfileResponseDto mapToProfileDto(Course course) {
        return CourseProfileResponseDto.builder()
                .id(course.getId())
                .title(course.getTitle())
                .subtitle(course.getSubtitle())
                .description(course.getDescription())
                .category(course.getCategory())
                .createdAt(course.getCreatedAt() != null ? course.getCreatedAt().toString() : null)
                .updatedAt(course.getUpdatedAt() != null ? course.getUpdatedAt().toString() : null)
                .build();
    }
}
