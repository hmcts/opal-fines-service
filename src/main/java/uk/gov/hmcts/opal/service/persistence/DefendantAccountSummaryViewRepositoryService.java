package uk.gov.hmcts.opal.service.persistence;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountSummaryViewEntity;
import uk.gov.hmcts.opal.repository.DefendantAccountSummaryViewRepository;

@Service
@Slf4j(topic = "opal.DefendantAccountSummaryViewService")
@RequiredArgsConstructor
public class DefendantAccountSummaryViewRepositoryService {

    private final DefendantAccountSummaryViewRepository repository;

    public DefendantAccountSummaryViewEntity getSummaryViewById(Long defendantAccountId) {
        return repository.findById(defendantAccountId)
            .orElseThrow(() -> new EntityNotFoundException("Defendant Account not found with id: "
                + defendantAccountId));
    }
}
