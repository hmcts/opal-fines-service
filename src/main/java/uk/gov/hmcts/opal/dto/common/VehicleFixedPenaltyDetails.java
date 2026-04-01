package uk.gov.hmcts.opal.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(Include.ALWAYS)
public class VehicleFixedPenaltyDetails {

    @Size(min = 1)
    @JsonProperty("vehicle_registration_number")
    private String vehicleRegistrationNumber;

    @Size(min = 1)
    @JsonProperty("vehicle_drivers_license")
    private String vehicleDriversLicense;

    @Size(min = 1)
    @JsonProperty("notice_number")
    private String noticeNumber;

    @Size(min = 1)
    @JsonProperty("date_notice_issued")
    private String dateNoticeIssued;
}
