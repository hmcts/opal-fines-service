package uk.gov.hmcts.opal.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class OpalS2SRequestWrapper {

    @JsonProperty("external_api_payload")
    String externalApiPayload;

}
