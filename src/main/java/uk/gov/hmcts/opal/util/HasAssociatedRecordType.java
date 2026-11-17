package uk.gov.hmcts.opal.util;

import uk.gov.hmcts.opal.entity.AssociatedRecordType;

public interface HasAssociatedRecordType extends Versioned {

    AssociatedRecordType getAssociatedRecordType();

    long getId();
}
