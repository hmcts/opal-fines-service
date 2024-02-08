package uk.gov.hmcts.opal.service;

import uk.gov.hmcts.opal.dto.search.AccountTransferSearchDto;
import uk.gov.hmcts.opal.entity.AccountTransferEntity;

import java.util.List;

public interface AccountTransferServiceInterface {

    AccountTransferEntity getAccountTransfer(long accountTransferId);

    List<AccountTransferEntity> searchAccountTransfers(AccountTransferSearchDto criteria);
}
