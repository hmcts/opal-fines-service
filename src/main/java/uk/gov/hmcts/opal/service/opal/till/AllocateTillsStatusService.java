package uk.gov.hmcts.opal.service.opal.till;

import static uk.gov.hmcts.opal.entity.TillStatusEnum.Processing;
import static uk.gov.hmcts.opal.entity.TillStatusEnum.Failed;

import jakarta.persistence.EntityNotFoundException;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.repository.TillRepository;

@Service
@RequiredArgsConstructor
public class AllocateTillsStatusService {

    private final TillRepository tillRepository;

    @Transactional(readOnly = true)
    public boolean isProcessing(Long tillId) {
        return tillRepository.findById(tillId)
            .map(till -> till.getStatus() == Processing)
            .orElse(false);
    }

    @Transactional
    public void markFailed(Long tillId) {
        tillRepository.findById(tillId)
            .orElseThrow(() -> new EntityNotFoundException("Till not found with id: " + tillId))
            .setStatus(Failed);
    }
}
