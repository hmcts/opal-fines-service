package uk.gov.hmcts.opal.service.opal;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.MiscellaneousAccountSearchDto;
import uk.gov.hmcts.opal.entity.MiscellaneousAccountEntity;
import uk.gov.hmcts.opal.repository.MiscellaneousAccountRepository;
import uk.gov.hmcts.opal.repository.jpa.MiscellaneousAccountSpecs;
import uk.gov.hmcts.opal.service.MiscellaneousAccountServiceInterface;

import java.util.List;

@Service
@RequiredArgsConstructor
@Qualifier("miscellaneousAccountService")
public class MiscellaneousAccountService implements MiscellaneousAccountServiceInterface {

    private final MiscellaneousAccountRepository miscellaneousAccountRepository;

    private final MiscellaneousAccountSpecs specs = new MiscellaneousAccountSpecs();

    @Override
    public MiscellaneousAccountEntity getMiscellaneousAccount(long miscellaneousAccountId) {
        return miscellaneousAccountRepository.getReferenceById(miscellaneousAccountId);
    }

    @Override
    public List<MiscellaneousAccountEntity> searchMiscellaneousAccounts(MiscellaneousAccountSearchDto criteria) {
        Page<MiscellaneousAccountEntity> page = miscellaneousAccountRepository
            .findBy(specs.findBySearchCriteria(criteria),
                    ffq -> ffq.page(Pageable.unpaged()));

        return page.getContent();
    }

}
