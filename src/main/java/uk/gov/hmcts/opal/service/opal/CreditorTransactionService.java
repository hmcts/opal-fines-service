package uk.gov.hmcts.opal.service.opal;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.CreditorTransactionSearchDto;
import uk.gov.hmcts.opal.entity.CreditorTransactionEntity;
import uk.gov.hmcts.opal.repository.CreditorTransactionRepository;
import uk.gov.hmcts.opal.repository.jpa.CreditorTransactionSpecs;
import uk.gov.hmcts.opal.service.CreditorTransactionServiceInterface;

import java.util.List;

@Service
@RequiredArgsConstructor
@Qualifier("creditorTransactionService")
public class CreditorTransactionService implements CreditorTransactionServiceInterface {

    private final CreditorTransactionRepository creditorTransactionRepository;

    private final CreditorTransactionSpecs specs = new CreditorTransactionSpecs();

    @Override
    public CreditorTransactionEntity getCreditorTransaction(long creditorTransactionId) {
        return creditorTransactionRepository.getReferenceById(creditorTransactionId);
    }

    @Override
    public List<CreditorTransactionEntity> searchCreditorTransactions(CreditorTransactionSearchDto criteria) {
        Page<CreditorTransactionEntity> page = creditorTransactionRepository
            .findBy(specs.findBySearchCriteria(criteria),
                    ffq -> ffq.page(Pageable.unpaged()));

        return page.getContent();
    }

}
