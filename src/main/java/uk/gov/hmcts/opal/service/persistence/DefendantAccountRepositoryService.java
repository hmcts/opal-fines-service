package uk.gov.hmcts.opal.service.persistence;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.repository.DefendantAccountRepository;

@Service
@Slf4j(topic = "opal.DefendantAccountRepositoryService")
@RequiredArgsConstructor
public class DefendantAccountRepositoryService {

    private final DefendantAccountRepository defendantAccountRepository;

    @Transactional(readOnly = true)
    public DefendantAccountEntity findById(long defendantAccountId) {
        return defendantAccountRepository.findById(defendantAccountId)
            .orElseThrow(
                () -> new EntityNotFoundException("Defendant Account not found with id: " + defendantAccountId));
    }

    @Transactional(readOnly = true)
    public Optional<DefendantAccountEntity> findByDefendantAccountId(long defendantAccountId) {
        return defendantAccountRepository.findByDefendantAccountId(defendantAccountId);
    }

    @Transactional(readOnly = true)
    public List<DefendantAccountEntity> findAllById(Iterable<Long> defendantAccountIds) {
        return defendantAccountRepository.findAllById(defendantAccountIds);
    }

    @Transactional
    public DefendantAccountEntity saveAndFlush(DefendantAccountEntity defendantAccountEntity) {
        return defendantAccountRepository.saveAndFlush(defendantAccountEntity);
    }

    @Transactional
    public DefendantAccountEntity save(DefendantAccountEntity defendantAccountEntity) {
        return defendantAccountRepository.save(defendantAccountEntity);
    }
}
