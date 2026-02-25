package uk.gov.hmcts.opal.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountEnquiryDto implements ToJsonString {

    @JsonProperty("businessUnitId")
    private Short businessUnitId;
    @JsonProperty("accountNumber")
    private String accountNumber;

    public String toString() {
        return toJson();
    }
}
