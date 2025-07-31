package uk.gov.hmcts.opal.disco.opal;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.SuspenseAccountSearchDto;
import uk.gov.hmcts.opal.entity.SuspenseAccountEntity;
import uk.gov.hmcts.opal.repository.SuspenseAccountRepository;
import uk.gov.hmcts.opal.repository.jpa.SuspenseAccountSpecs;
import uk.gov.hmcts.opal.disco.SuspenseAccountServiceInterface;

import java.util.List;

@Service
@RequiredArgsConstructor
@Qualifier("suspenseAccountService")
public class SuspenseAccountService implements SuspenseAccountServiceInterface {

    private final SuspenseAccountRepository suspenseAccountRepository;

    private final SuspenseAccountSpecs specs = new SuspenseAccountSpecs();

    @Override
    public SuspenseAccountEntity getSuspenseAccount(long suspenseAccountId) {
        return suspenseAccountRepository.getReferenceById(suspenseAccountId);
    }

    @Override
    public List<SuspenseAccountEntity> searchSuspenseAccounts(SuspenseAccountSearchDto criteria) {
        Page<SuspenseAccountEntity> page = suspenseAccountRepository
            .findBy(specs.findBySearchCriteria(criteria),
                    ffq -> ffq.page(Pageable.unpaged()));

        return page.getContent();
    }

}
