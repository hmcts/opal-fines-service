package uk.gov.hmcts.opal.service.iface;

import uk.gov.hmcts.opal.common.user.authorisation.model.BusinessUnitUserV2;
import uk.gov.hmcts.opal.entity.draft.DraftAccountEntity;

public interface DraftAccountPublishInterface {
    DraftAccountEntity publishDefendantAccount(DraftAccountEntity updatedEntity, BusinessUnitUserV2 unitUser);
}
