package uk.gov.hmcts.opal.service.opal;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.SuspenseTransactionSearchDto;
import uk.gov.hmcts.opal.entity.SuspenseTransactionEntity;
import uk.gov.hmcts.opal.repository.SuspenseTransactionRepository;
import uk.gov.hmcts.opal.repository.jpa.SuspenseTransactionSpecs;
import uk.gov.hmcts.opal.service.SuspenseTransactionServiceInterface;

import java.util.List;

@Service
@RequiredArgsConstructor
@Qualifier("suspenseTransactionService")
public class SuspenseTransactionService implements SuspenseTransactionServiceInterface {

    private final SuspenseTransactionRepository suspenseTransactionRepository;

    private final SuspenseTransactionSpecs specs = new SuspenseTransactionSpecs();

    @Override
    public SuspenseTransactionEntity getSuspenseTransaction(long suspenseTransactionId) {
        return suspenseTransactionRepository.getReferenceById(suspenseTransactionId);
    }

    @Override
    public List<SuspenseTransactionEntity> searchSuspenseTransactions(SuspenseTransactionSearchDto criteria) {
        Page<SuspenseTransactionEntity> page = suspenseTransactionRepository
            .findBy(specs.findBySearchCriteria(criteria),
                    ffq -> ffq.page(Pageable.unpaged()));

        return page.getContent();
    }

}
