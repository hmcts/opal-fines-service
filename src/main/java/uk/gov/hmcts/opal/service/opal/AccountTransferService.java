package uk.gov.hmcts.opal.service.opal;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.AccountTransferSearchDto;
import uk.gov.hmcts.opal.entity.AccountTransferEntity;
import uk.gov.hmcts.opal.repository.AccountTransferRepository;
import uk.gov.hmcts.opal.service.AccountTransferServiceInterface;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountTransferService implements AccountTransferServiceInterface {

    private final AccountTransferRepository accountTransferRepository;

    @Override
    public AccountTransferEntity getAccountTransfer(long accountTransferId) {
        return accountTransferRepository.getReferenceById(accountTransferId);
    }

    @Override
    public List<AccountTransferEntity> searchAccountTransfers(AccountTransferSearchDto criteria) {
        return null;
    }

}
