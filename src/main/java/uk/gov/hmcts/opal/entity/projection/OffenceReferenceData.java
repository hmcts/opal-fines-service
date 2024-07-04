package uk.gov.hmcts.opal.entity.projection;

public record OffenceReferenceData(Long offenceId, String getCjsCode, Short businessUnitId,
    String getOffenceTitle, String getOffenceTitleCy) {
}
