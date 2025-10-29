package uk.gov.hmcts.opal.dto.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleFixedPenaltyDetails {

    @JsonProperty("vehicle_registration_number")
    private String vehicleRegistrationNumber;

    @JsonProperty("vehicle_drivers_license")
    private String vehicleDriversLicense;

    @JsonProperty("notice_number")
    private String noticeNumber;

    @JsonProperty("date_notice_issued")
    private String dateNoticeIssued;
}
