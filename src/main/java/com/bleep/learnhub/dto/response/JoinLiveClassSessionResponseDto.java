package com.bleep.learnhub.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JoinLiveClassSessionResponseDto {

    private SessionDto session;
    private PartnerDto partnerDetails;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SessionDto {
        private String id;
        private String courseId;
        private String courseName;
        private String batchId;
        private String batchName;
        private String sessionType;
        private String title;
        private String subtitle;
        private String liveLink;
        private String resourceLink;
        private String scheduledDate;
        private String scheduledTime;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PartnerDto {
        private String id;
        private String companyName;
    }
}
