package uk.gov.hmcts.opal.service.persistence;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountHeaderViewEntity;
import uk.gov.hmcts.opal.repository.DefendantAccountHeaderViewRepository;

@Service
@Slf4j(topic = "opal.DefendantAccountHeaderViewService")
@RequiredArgsConstructor
public class DefendantAccountHeaderViewRepositoryService {

    private final DefendantAccountHeaderViewRepository repository;

    public DefendantAccountHeaderViewEntity getHeaderViewById(Long defendantAccountId) {
        return repository.findById(defendantAccountId)
            .orElseThrow(() -> new EntityNotFoundException("Defendant Account not found with id: "
                + defendantAccountId));
    }
}
