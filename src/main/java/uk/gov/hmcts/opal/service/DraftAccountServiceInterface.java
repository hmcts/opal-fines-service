package uk.gov.hmcts.opal.service;

import uk.gov.hmcts.opal.dto.search.DraftAccountSearchDto;
import uk.gov.hmcts.opal.entity.DraftAccountEntity;

import java.util.List;

public interface DraftAccountServiceInterface {

    DraftAccountEntity getDraftAccount(long draftAccountId);

    List<DraftAccountEntity> searchDraftAccounts(DraftAccountSearchDto criteria);
}
