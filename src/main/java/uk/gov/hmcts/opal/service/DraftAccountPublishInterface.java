package uk.gov.hmcts.opal.service;

import uk.gov.hmcts.opal.authorisation.model.BusinessUnitUser;
import uk.gov.hmcts.opal.entity.draft.DraftAccountEntity;

public interface DraftAccountPublishInterface {
    DraftAccountEntity publishDefendantAccount(DraftAccountEntity updatedEntity, BusinessUnitUser unitUser);
}
