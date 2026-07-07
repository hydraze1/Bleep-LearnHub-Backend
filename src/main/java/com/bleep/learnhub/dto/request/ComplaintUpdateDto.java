package com.bleep.learnhub.dto.request;

import com.bleep.learnhub.entity.enums.ComplaintStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ComplaintUpdateDto {

    @NotNull(message = "Status is required")
    private ComplaintStatus status;

    private String vendorRemark;

    private String partnerRemark;
}
