package com.bleep.learnhub.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object containing device details sent from frontend headers.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceDetailsDto {
    private String deviceIp;
    private String deviceType;
    private String device;
    private String deviceModel;
    private String osName;
    private String osVersion;
    private String clientName;
    private String clientVersion;
}
