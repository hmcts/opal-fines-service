package uk.gov.hmcts.opal.entity.projection;

import java.time.LocalDateTime;

public record OffenceReferenceData(Long offenceId, String getCjsCode, Short businessUnitId,
    String getOffenceTitle, String getOffenceTitleCy, LocalDateTime dateUsedFrom, LocalDateTime dateUsedTo,
    String offenceOas, String offenceOasCy) {
}
