package uk.gov.hmcts.opal.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OpalS2SResponseWrapper {

    String opalResponsePayload;

    String errorDetail;
}
