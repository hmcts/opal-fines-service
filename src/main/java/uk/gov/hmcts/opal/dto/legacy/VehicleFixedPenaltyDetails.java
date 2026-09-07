package uk.gov.hmcts.opal.dto.legacy;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VehicleFixedPenaltyDetails {

    @JsonProperty("fp_registration_number")
    private String fpRegistrationNumber;

    @JsonProperty("fp_driving_license")
    private String fpDrivingLicense;

    @JsonProperty("notice_to_owner_or_hirer_number")
    private String noticeToOwnerOrHirerNumber;

    @JsonProperty("date_notice_to_owner_was_issued")
    private String dateNoticeIssued;

}
