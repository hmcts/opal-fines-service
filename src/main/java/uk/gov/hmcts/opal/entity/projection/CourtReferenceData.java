package uk.gov.hmcts.opal.entity.projection;

public record CourtReferenceData(Long courtId, Short businessUnitId, Short courtCode, String name,
    String nameCy, String nationalCourtCode) {

}
