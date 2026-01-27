package uk.gov.hmcts.opal.service.persistence;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.entity.DefendantAccountEntity;
import uk.gov.hmcts.opal.repository.DefendantAccountRepository;

@Service
@Slf4j(topic = "opal.DefendantAccountRepositoryService")
@RequiredArgsConstructor
public class DefendantAccountRepositoryService {

    private final DefendantAccountRepository defendantAccountRepository;

    @Transactional(readOnly = true)
    public DefendantAccountEntity findById(long defendantAccountId) {
        return defendantAccountRepository
            .findById(defendantAccountId)
            .orElseThrow(
                () ->
                    new EntityNotFoundException(
                        "Defendant Account not found with id: " + defendantAccountId));
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
