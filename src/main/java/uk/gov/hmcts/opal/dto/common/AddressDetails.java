package uk.gov.hmcts.opal.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
public class AddressDetails {

    @JsonProperty("address_line_1")
    private String addressLine1;

    @JsonProperty("address_line_2")
    private String addressLine2;

    @JsonProperty("address_line_3")
    private String addressLine3;

    @JsonInclude(JsonInclude.Include.ALWAYS)
    @JsonProperty("address_line_4")
    private String addressLine4;

    @JsonInclude(JsonInclude.Include.ALWAYS)
    @JsonProperty("address_line_5")
    private String addressLine5;

    @JsonProperty("postcode")
    private String postcode;
}
