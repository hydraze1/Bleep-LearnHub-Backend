package com.bleep.learnhub.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Async
    public void sendWelcomeEmail(String to, String username, String role) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("Welcome to Bleep LearnHub!");
            message.setText("Hello,\n\nYour " + role + " account has been created. " +
                    "Your username is: " + username + "\n\n" +
                    "Please use the app to send yourself an OTP and set up your password.");
            mailSender.send(message);
            log.info("Welcome email sent successfully to {}", to);
        } catch (Exception e) {
            log.error("Failed to send welcome email to {}: {}", to, e.getMessage());
        }
    }

    @Async
    public void sendOtpEmail(String to, String otp) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("Your Bleep LearnHub OTP");
            message.setText("Your One-Time Password is: " + otp +
                    "\n\nThis OTP will expire in 30 minutes. Do not share it with anyone.");
            mailSender.send(message);
            log.info("OTP email sent successfully to {}", to);
        } catch (Exception e) {
            log.error("Failed to send OTP email to {}: {}", to, e.getMessage());
        }
    }

    @Async
    public void sendForgotUsernameEmail(String to, String username) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("Your Bleep LearnHub Username");
            message.setText("Hello,\n\nYour username for Bleep LearnHub is: " + username +
                    "\n\nIf you did not request this, please ignore this email.");
            mailSender.send(message);
            log.info("Forgot-username email sent successfully to {}", to);
        } catch (Exception e) {
            log.error("Failed to send forgot-username email to {}: {}", to, e.getMessage());
        }
    }
}