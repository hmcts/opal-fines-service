package uk.gov.hmcts.opal.dto;

import uk.gov.hmcts.opal.logging.integration.dto.IdentifierType;

public enum PdplIdentifierType implements IdentifierType {

    DRAFT_ACCOUNT,
    OPAL_USER_ID,
    DEBTOR_ACCOUNT,
    PARTY_NAME,
    NINO,
    DOB,
    ACCOUNT_NUMBER,
    ORGANISATION,
    WARRANT_REFERENCE,
    PROSECUTOR_CASE_REFERENCE;

    @Override
    public String getType() {
        return this.name();
    }
}
