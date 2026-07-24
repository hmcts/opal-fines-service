package uk.gov.hmcts.opal.service.persistence;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.entity.CreditorTransactionEntity;
import uk.gov.hmcts.opal.repository.CreditorTransactionRepository;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "opal.CreditorTransactionRepositoryService")
public class CreditorTransactionRepositoryService {

    private final CreditorTransactionRepository creditorTransactionRepository;

    @Transactional(readOnly = true)
    public List<CreditorTransactionEntity> findAll(Specification<CreditorTransactionEntity> specification) {
        return creditorTransactionRepository.findAll(specification);
    }
}
