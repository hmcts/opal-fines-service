package uk.gov.hmcts.opal.dto;

import uk.gov.hmcts.opal.logging.integration.dto.IdentifierType;

public enum PdplIdentifierType implements IdentifierType {

    DEFENDANT_ACCOUNT,
    CONSOLIDATED_ACCOUNT,
    DEBTOR_ACCOUNT,
    PARENT_GUARDIAN,
    DRAFT_ACCOUNT,
    OPAL_USER_ID;

    @Override
    public String getType() {
        return this.name();
    }
}
