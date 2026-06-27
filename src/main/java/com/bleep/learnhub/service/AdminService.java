package com.bleep.learnhub.service;

import com.bleep.learnhub.entity.User;
import com.bleep.learnhub.entity.Vendor;
import com.bleep.learnhub.entity.enums.AccountStatus;
import com.bleep.learnhub.entity.enums.Role;
import com.bleep.learnhub.repository.UserRepository;
import com.bleep.learnhub.repository.UserSessionRepository;
import com.bleep.learnhub.repository.VendorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final VendorRepository vendorRepository;
    private final UserSessionRepository userSessionRepository;
    private final EmailService emailService;

    @Transactional
    public void createVendor(String username, String email, String companyName) {
        if (userRepository.existsByUsername(username) || userRepository.existsByEmail(email)) {
            throw new RuntimeException("Username or Email already exists");
        }

        User user = User.builder()
                .username(username)
                .email(email)
                .role(Role.VENDOR)
                .status(AccountStatus.PENDING_SETUP)
                .build();
        userRepository.save(user);

        Vendor vendor = Vendor.builder()
                .user(user)
                .email(email)
                .companyName(companyName)
                .isActive(true)
                .build();
        vendorRepository.save(vendor);

        emailService.sendWelcomeEmail(email, username, "Vendor");
    }

    @Transactional
    public void changeVendorStatus(String vendorId, String statusStr) {
        UUID uuid = UUID.fromString(vendorId);
        Vendor vendor = vendorRepository.findById(uuid)
                .orElseThrow(() -> new RuntimeException("Vendor not found"));

        User user = vendor.getUser();
        AccountStatus status = AccountStatus.valueOf(statusStr);
        user.setStatus(status);
        userRepository.save(user);

        if (status == AccountStatus.BLOCKED) {
            userSessionRepository.invalidateAllSessionsForUser(user.getId());
        }
    }
}
