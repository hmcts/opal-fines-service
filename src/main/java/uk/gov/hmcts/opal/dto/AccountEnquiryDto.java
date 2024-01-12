package uk.gov.hmcts.opal.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class AccountEnquiryDto implements ToJsonString {

    @JsonProperty("businessUnitId")
    private Short businessUnitId;
    @JsonProperty("accountNumber")
    private String accountNumber;

    public String toString() {
        return toJson();
    }
}
