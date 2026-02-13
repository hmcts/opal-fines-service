package uk.gov.hmcts.opal.dto.reference;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public interface LjaReferenceData {

    @JsonProperty("local_justice_area_id")
    Short getLocalJusticeAreaId();

    @JsonProperty("lja_code")
    String getLjaCode();

    @JsonProperty("lja_type")
    String getLjaType();

    @JsonProperty("name")
    String getName();

    @JsonProperty("address_line_1")
    String getAddressLine1();

    @JsonProperty("postcode")
    String getPostcode();
}
