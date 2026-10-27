package uk.gov.hmcts.opal.dto.legacy;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class VehicleFixedPenaltyDetails {

    @XmlElement(name = "fp_registration_number")
    @JsonProperty("fp_registration_number")
    private String fpRegistrationNumber;

    @XmlElement(name = "fp_driving_license")
    @JsonProperty("fp_driving_license")
    private String fpDrivingLicense;

    @XmlElement(name = "notice_to_owner_or_hirer_number")
    @JsonProperty("notice_to_owner_or_hirer_number")
    private String noticeToOwnerOrHirerNumber;

    @XmlElement(name = "date_notice_to_owner_was_issued")
    @JsonProperty("date_notice_to_owner_was_issued")
    private String dateNoticeIssued;

}
