package uk.gov.hmcts.opal.disco.opal;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.DefendantTransactionSearchDto;
import uk.gov.hmcts.opal.entity.DefendantTransactionEntity;
import uk.gov.hmcts.opal.repository.DefendantTransactionRepository;
import uk.gov.hmcts.opal.repository.jpa.DefendantTransactionSpecs;
import uk.gov.hmcts.opal.disco.DefendantTransactionServiceInterface;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "opal.DefendantTransactionService")
@Qualifier("defendantTransactionService")
public class DefendantTransactionService implements DefendantTransactionServiceInterface {

    private final DefendantTransactionRepository defendantTransactionRepository;

    private final DefendantTransactionSpecs specs = new DefendantTransactionSpecs();

    @Override
    public DefendantTransactionEntity getDefendantTransaction(long defendantTransactionId) {
        return defendantTransactionRepository.getReferenceById(defendantTransactionId);
    }

    @Override
    public List<DefendantTransactionEntity> searchDefendantTransactions(DefendantTransactionSearchDto criteria) {
        log.debug(":searchDefendantTransactions: criteria: {}", criteria);
        Page<DefendantTransactionEntity> page = defendantTransactionRepository
            .findBy(specs.findBySearchCriteria(criteria),
                    ffq -> ffq.page(Pageable.unpaged()));

        return page.getContent();
    }

}
