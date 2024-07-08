package uk.gov.hmcts.opal.service.opal;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.DraftAccountSearchDto;
import uk.gov.hmcts.opal.entity.DraftAccountEntity;
import uk.gov.hmcts.opal.repository.DraftAccountRepository;
import uk.gov.hmcts.opal.repository.jpa.DraftAccountSpecs;
import uk.gov.hmcts.opal.service.DraftAccountServiceInterface;

import java.util.List;

@Service
@RequiredArgsConstructor
@Qualifier("draftAccountService")
public class DraftAccountService implements DraftAccountServiceInterface {

    private final DraftAccountRepository draftAccountRepository;

    private final DraftAccountSpecs specs = new DraftAccountSpecs();

    @Override
    public DraftAccountEntity getDraftAccount(long draftAccountId) {
        return draftAccountRepository.getReferenceById(draftAccountId);
    }

    @Override
    public List<DraftAccountEntity> searchDraftAccounts(DraftAccountSearchDto criteria) {
        Page<DraftAccountEntity> page = draftAccountRepository
            .findBy(specs.findBySearchCriteria(criteria),
                    ffq -> ffq.page(Pageable.unpaged()));

        return page.getContent();
    }

}
