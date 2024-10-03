package uk.gov.hmcts.opal.entity.projection;

import com.fasterxml.jackson.annotation.JsonProperty;

public interface LjaReferenceData {

    @JsonProperty("local_justice_area_id")
    Short getLocalJusticeAreaId();

    @JsonProperty("lja_code")
    String getLjaCode();

    @JsonProperty("name")
    String getName();

    @JsonProperty("address_line_1")
    String getAddressLine1();

    @JsonProperty("postcode")
    String getPostcode();
}
