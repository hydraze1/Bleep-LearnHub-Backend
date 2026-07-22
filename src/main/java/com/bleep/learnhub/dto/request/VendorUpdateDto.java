package com.bleep.learnhub.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VendorUpdateDto {
    @NotBlank(message = "Company name is required")
    private String companyName;

    private String phone;

    private String description;

    private Boolean isActive;
}
