package uk.gov.hmcts.opal.entity.projection;

public record MajorCreditorReferenceData(Long majorCreditorId, Short businessUnitId,
    String majorCreditorCode, String name, String postcode) {

}
