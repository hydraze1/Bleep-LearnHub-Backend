package com.bleep.learnhub.service;

import com.bleep.learnhub.entity.AuditLog;
import com.bleep.learnhub.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public Page<AuditLog> getAllLogs(Pageable pageable, String search) {
        if (search == null || search.trim().isEmpty()) {
            return auditLogRepository.findAll(pageable);
        }
        String searchLower = "%" + search.toLowerCase() + "%";
        Specification<AuditLog> spec = (root, query, criteriaBuilder) -> criteriaBuilder.or(
                criteriaBuilder.like(criteriaBuilder.lower(root.get("action")), searchLower),
                criteriaBuilder.like(criteriaBuilder.lower(root.get("entityName")), searchLower),
                criteriaBuilder.like(criteriaBuilder.lower(root.get("ipAddress")), searchLower)
        );
        return auditLogRepository.findAll(spec, pageable);
    }

    public void deleteLog(UUID id) {
        auditLogRepository.deleteById(id);
    }

    public void deleteAllLogs() {
        auditLogRepository.deleteAll();
    }
}
