package com.bleep.learnhub.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Flat representation of the Partner entity row.
 * Returned for users with PARTNER role in login/session responses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartnerDataDto {

    private String id;
    private String companyName;
    private String phone;
    private String description;
    private boolean active;
    /** ID of the parent Vendor this partner belongs to. */
    private String vendorId;
    private String createdAt;
    private DeviceDetailsDto deviceDetails;
}
