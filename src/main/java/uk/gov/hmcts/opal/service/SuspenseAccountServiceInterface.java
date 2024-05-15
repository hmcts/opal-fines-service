package uk.gov.hmcts.opal.service;

import uk.gov.hmcts.opal.dto.search.SuspenseAccountSearchDto;
import uk.gov.hmcts.opal.entity.SuspenseAccountEntity;

import java.util.List;

public interface SuspenseAccountServiceInterface {

    SuspenseAccountEntity getSuspenseAccount(long suspenseAccountId);

    List<SuspenseAccountEntity> searchSuspenseAccounts(SuspenseAccountSearchDto criteria);
}
