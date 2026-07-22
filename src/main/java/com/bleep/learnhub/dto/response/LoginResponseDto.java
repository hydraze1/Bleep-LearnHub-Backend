package com.bleep.learnhub.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The primary login / session response payload.
 * Always contains the user row. Vendor and Partner rows are included
 * based on the authenticated user's role:
 * <ul>
 *   <li>SUPER_ADMIN → user only</li>
 *   <li>VENDOR      → user + vendor</li>
 *   <li>PARTNER     → user + vendor (parent) + partner</li>
 * </ul>
 *
 * This object is also serialised as JSON and stored in Redis as the
 * session payload, so that /session can return the same shape without
 * hitting the database on every request.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoginResponseDto {

    private UserDataDto user;
    private VendorDataDto vendor;
    private PartnerDataDto partner;
}
