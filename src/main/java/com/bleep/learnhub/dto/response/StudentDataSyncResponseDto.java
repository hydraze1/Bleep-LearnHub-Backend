package com.bleep.learnhub.dto.response;

import com.bleep.learnhub.entity.Student;
import com.bleep.learnhub.entity.StudentEnrollment;
import com.bleep.learnhub.entity.StudentSessionLog;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StudentDataSyncResponseDto {
    private Student student;
    private StudentEnrollment enrollment;
    private StudentSessionLog sessionLog;
}
