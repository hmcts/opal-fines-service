package uk.gov.hmcts.opal.service;

import uk.gov.hmcts.opal.dto.search.CreditorAccountSearchDto;
import uk.gov.hmcts.opal.entity.CreditorAccountEntity;

import java.util.List;

public interface CreditorAccountServiceInterface {

    CreditorAccountEntity getCreditorAccount(long creditorAccountId);

    List<CreditorAccountEntity> searchCreditorAccounts(CreditorAccountSearchDto criteria);
}
