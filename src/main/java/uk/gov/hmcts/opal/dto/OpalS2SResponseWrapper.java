package uk.gov.hmcts.opal.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OpalS2SResponseWrapper {

    @JsonProperty("opal_response_payload")
    String opalResponsePayload;

    @JsonProperty("error_detail")
    String errorDetail;
}
