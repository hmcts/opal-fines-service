package uk.gov.hmcts.opal.service.opal;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.AccountTransferSearchDto;
import uk.gov.hmcts.opal.entity.AccountTransferEntity;
import uk.gov.hmcts.opal.repository.AccountTransferRepository;
import uk.gov.hmcts.opal.repository.jpa.AccountTransferSpecs;
import uk.gov.hmcts.opal.service.AccountTransferServiceInterface;

import java.util.List;

@Service
@RequiredArgsConstructor
@Qualifier("accountTransferService")
public class AccountTransferService implements AccountTransferServiceInterface {

    private final AccountTransferRepository accountTransferRepository;

    private final AccountTransferSpecs specs = new AccountTransferSpecs();

    @Override
    public AccountTransferEntity getAccountTransfer(long accountTransferId) {
        return accountTransferRepository.getReferenceById(accountTransferId);
    }

    @Override
    public List<AccountTransferEntity> searchAccountTransfers(AccountTransferSearchDto criteria) {
        Page<AccountTransferEntity> page = accountTransferRepository
            .findBy(specs.findBySearchCriteria(criteria),
                    ffq -> ffq.page(Pageable.unpaged()));

        return page.getContent();
    }

}
