package uk.gov.hmcts.opal.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OpalS2SResponseWrapper {

    @JsonProperty("opal_response_payload")
    String opalResponsePayload;

    @JsonProperty("error_detail")
    String errorDetail;
}
