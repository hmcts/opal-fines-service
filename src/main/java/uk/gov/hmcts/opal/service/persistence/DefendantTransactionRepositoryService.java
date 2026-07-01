package uk.gov.hmcts.opal.service.persistence;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.entity.defendanttransaction.DefendantTransactionEntity;
import uk.gov.hmcts.opal.repository.DefendantTransactionRepository;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "opal.DefendantTransactionRepositoryService")
public class DefendantTransactionRepositoryService {

    private final DefendantTransactionRepository defendantTransactionRepository;

    @Transactional(readOnly = true)
    public List<DefendantTransactionEntity> findAll(Specification<DefendantTransactionEntity> specification) {
        return defendantTransactionRepository.findAll(specification);
    }
}
