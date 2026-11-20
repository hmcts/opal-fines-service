package uk.gov.hmcts.opal.service.opal.till;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.entity.TillEntity;
import uk.gov.hmcts.opal.repository.PaymentInRepository;
import uk.gov.hmcts.opal.repository.TillRepository;
import java.util.Objects;
import uk.gov.hmcts.opal.exception.InvalidReferenceValidationException;

@Service
@RequiredArgsConstructor
@Slf4j
public class TestingSupportTillService {

    private final TillRepository tillRepository;
    private final PaymentInRepository paymentInRepository;

    @Transactional
    public void deleteManualTills(List<Long> tillIds) {
        if (tillIds == null || tillIds.isEmpty()
            || tillIds.stream().anyMatch(Objects::isNull)) {
            throw new InvalidReferenceValidationException("At least one till id must be supplied");
        }

        List<Long> distinctIds = tillIds.stream().distinct().toList();

        List<Long> manualTillIds = tillRepository.findAllById(distinctIds).stream()
            .filter(till -> !till.isAutoPayment())
            .map(TillEntity::getTillId)
            .toList();

        if (manualTillIds.isEmpty()) {
            return;
        }

        log.warn("DESTRUCTIVE OPERATION: Deleting manual tills with ids: {}", manualTillIds);

        paymentInRepository.deleteByTillEntity_TillIdIn(manualTillIds);
        tillRepository.deleteAllById(manualTillIds);
    }
}