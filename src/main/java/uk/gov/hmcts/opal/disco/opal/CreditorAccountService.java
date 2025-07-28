package uk.gov.hmcts.opal.disco.opal;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.CreditorAccountSearchDto;
import uk.gov.hmcts.opal.entity.creditoraccount.CreditorAccountEntity;
import uk.gov.hmcts.opal.repository.CreditorAccountRepository;
import uk.gov.hmcts.opal.repository.jpa.CreditorAccountSpecs;
import uk.gov.hmcts.opal.disco.CreditorAccountServiceInterface;

import java.util.List;

@Service
@RequiredArgsConstructor
@Qualifier("creditorAccountService")
public class CreditorAccountService implements CreditorAccountServiceInterface {

    private final CreditorAccountRepository creditorAccountRepository;

    private final CreditorAccountSpecs specs = new CreditorAccountSpecs();

    @Override
    public CreditorAccountEntity getCreditorAccount(long creditorAccountId) {
        return creditorAccountRepository.getReferenceById(creditorAccountId);
    }

    @Override
    public List<CreditorAccountEntity> searchCreditorAccounts(CreditorAccountSearchDto criteria) {
        Page<CreditorAccountEntity> page = creditorAccountRepository
            .findBy(specs.findBySearchCriteria(criteria),
                    ffq -> ffq.page(Pageable.unpaged()));

        return page.getContent();
    }

}
