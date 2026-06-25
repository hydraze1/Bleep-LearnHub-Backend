package com.bleep.learnhub.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Flat representation of the Vendor entity row.
 * Returned for users with VENDOR or PARTNER role in login/session responses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VendorDataDto {

    private String id;
    private String companyName;
    private String phone;
    private String description;
    private boolean active;
    private String createdAt;
}
