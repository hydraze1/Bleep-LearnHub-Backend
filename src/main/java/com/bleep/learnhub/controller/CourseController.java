package com.bleep.learnhub.controller;

import com.bleep.learnhub.dto.request.CourseCreateDto;
import com.bleep.learnhub.dto.request.CourseUpdateDto;
import com.bleep.learnhub.dto.response.ApiResponse;
import com.bleep.learnhub.dto.response.CourseDataDto;
import com.bleep.learnhub.dto.response.CourseProfileResponseDto;
import com.bleep.learnhub.service.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @PostMapping
    @PreAuthorize("hasAuthority('VENDOR')")
    public ResponseEntity<ApiResponse<CourseProfileResponseDto>> createCourse(@Valid @RequestBody CourseCreateDto dto) {
        CourseProfileResponseDto created = courseService.createCourse(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(created, "Course created successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('VENDOR')")
    public ResponseEntity<ApiResponse<Void>> updateCourse(
            @PathVariable UUID id,
            @Valid @RequestBody CourseUpdateDto dto) {
        courseService.updateCourse(id, dto);
        return ResponseEntity.ok(ApiResponse.success("Course updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('VENDOR')")
    public ResponseEntity<ApiResponse<Void>> deleteCourse(@PathVariable UUID id) {
        courseService.deleteCourse(id);
        return ResponseEntity.ok(ApiResponse.success("Course deleted successfully"));
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('VENDOR', 'PARTNER')")
    public ResponseEntity<ApiResponse<List<CourseDataDto>>> getAllCourses() {
        List<CourseDataDto> courses = courseService.getAllCourses();
        return ResponseEntity.ok(ApiResponse.success(courses, "Courses retrieved successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('VENDOR', 'PARTNER')")
    public ResponseEntity<ApiResponse<CourseProfileResponseDto>> getCourseById(@PathVariable UUID id) {
        CourseProfileResponseDto course = courseService.getCourseById(id);
        return ResponseEntity.ok(ApiResponse.success(course, "Course retrieved successfully"));
    }
}
