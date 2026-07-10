package uk.gov.hmcts.opal.entity;

import lombok.Getter;

@Getter
public enum SuspenseItemType {
    CC("Cancelled cheque (Fines)"),
    CF("Court Fee"),
    FA("Fines payment in advance"),
    FB("Cancelled BACS (Fines)"),
    IN("Adjustment"),
    LA("Legal aid payment"),
    MA("Maintenance payment"),
    MB("Cancelled BACS (Maintenance)"),
    MC("Cancelled cheque (Maintenance)"),
    MS("Miscellaneous"),
    OM("Overpayment (Maintenance)"),
    OC("Overpayment (Court Fees)"),
    OF("Overpayment (Fines)"),
    UN("Unidentified");

    private final String description;

    SuspenseItemType(final String description) {
        this.description = description;
    }
}
