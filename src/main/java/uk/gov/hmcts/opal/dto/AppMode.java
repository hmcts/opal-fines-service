package uk.gov.hmcts.opal.dto;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
public class AppMode {

    private String mode;

}
