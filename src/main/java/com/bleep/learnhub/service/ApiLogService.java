package com.bleep.learnhub.service;

import com.bleep.learnhub.entity.ApiLog;
import com.bleep.learnhub.repository.ApiLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ApiLogService {

    private final ApiLogRepository apiLogRepository;

    public Page<ApiLog> getAllLogs(Pageable pageable) {
        return apiLogRepository.findAll(pageable);
    }

    public void deleteLog(UUID id) {
        apiLogRepository.deleteById(id);
    }

    public void deleteAllLogs() {
        apiLogRepository.deleteAll();
    }
}
