package uk.gov.hmcts.opal.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigInteger;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.dto.common.AddressDetails;
import uk.gov.hmcts.opal.util.Versioned;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GetMajorCreditorAccountAtAGlanceResponse implements ToJsonString, Versioned {

    @JsonIgnore
    private BigInteger version;

    @JsonProperty("major_creditor")
    private MajorCreditor majorCreditor;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class MajorCreditor {

        @JsonProperty("creditor_account_id")
        private Long creditorAccountId;

        @JsonProperty("name")
        private String name;

        @JsonProperty("code")
        private String code;

        @JsonProperty("address")
        private AddressDetails address;

        @JsonProperty("pay_by_bacs")
        private Boolean payByBacs;
    }
}
