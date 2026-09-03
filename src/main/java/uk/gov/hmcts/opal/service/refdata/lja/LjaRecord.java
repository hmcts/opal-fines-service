package uk.gov.hmcts.opal.service.refdata.lja;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.dto.ToJsonString;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LjaRecord implements ToJsonString {

    @JsonProperty("LJACode")
    private String ljaCode;

    @JsonProperty("LJAName")
    private String ljaName;

    @JsonProperty("EndDate")
    private LocalDate endDate;

    @JsonProperty("CourtWelshName")
    private String courtWelshName;

    @JsonProperty("CourtLocationCode")
    private String courtLocationCode;

    @JsonProperty("StartDate")
    private LocalDate startDate;

    @JsonProperty("EnforcementCode")
    private String enforcementCode;

    @JsonProperty("ClusterCode")
    private String clusterCode;

    @JsonProperty("DivisionCode")
    private String divisionCode;

    @JsonProperty("DefaultStartTime")
    private String defaultStartTime;

    @JsonProperty("DefaultDuration")
    private Object defaultDuration;

    @JsonProperty("CommonPlatformUUID")
    private String commonPlatformUUID;

    @JsonProperty("Notes")
    private String notes;

    @JsonProperty("CourtHearingOperationAreaIndicator")
    private Boolean courtHearingOperationAreaIndicator;

    @JsonProperty("CrownCourtIndicator")
    private Boolean crownCourtIndicator;

    @JsonProperty("NorthernIrelandCourtIndicator")
    private Boolean northernIrelandCourtIndicator;

    @JsonProperty("MagistratesCourtIndicator")
    private Boolean magistratesCourtIndicator;

    @JsonProperty("ScottishDistrictCourtIndicator")
    private Boolean scottishDistrictCourtIndicator;

    @JsonProperty("ScottishSheriffCourtIndicator")
    private Boolean scottishSheriffCourtIndicator;

    @JsonProperty("ScottishJusticeOfPeaceCourtIndicator")
    private Boolean scottishJusticeOfPeaceCourtIndicator;

    @JsonProperty("YouthCourtIndicator")
    private Boolean youthCourtIndicator;

    @JsonProperty("Addresses")
    private List<Address> addresses;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Address implements ToJsonString {

        @JsonProperty("AddressType")
        private String addressType;

        @JsonProperty("AddressLine1")
        private String addressLine1;

        @JsonProperty("AddressLine2")
        private String addressLine2;

        @JsonProperty("AddressLine3")
        private String addressLine3;

        @JsonProperty("AddressLine4")
        private String addressLine4;

        @JsonProperty("Postcode")
        private String postcode;
    }
}
