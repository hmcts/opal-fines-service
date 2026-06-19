package uk.gov.hmcts.opal.dto.reference;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LjaReferenceData {

    @JsonProperty("local_justice_area_id")
    private Short localJusticeAreaId;

    @JsonProperty("lja_code")
    private String ljaCode;

    @JsonProperty("lja_type")
    private String ljaType;

    @JsonProperty("name")
    private String name;

    @JsonProperty("address_line_1")
    private String addressLine1;

    @JsonProperty("postcode")
    private String postcode;
}
