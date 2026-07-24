package uk.gov.hmcts.opal.service;

import uk.gov.hmcts.opal.entity.AssociatedRecordType;
import uk.gov.hmcts.opal.util.Versioned;

public record AccountNoteContext(
    Class<? extends Versioned> accountClass,
    Long accountId,
    Short businessUnitId,
    AssociatedRecordType associatedRecordType
) {
}
