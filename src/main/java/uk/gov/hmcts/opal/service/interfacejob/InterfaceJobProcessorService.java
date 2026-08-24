package uk.gov.hmcts.opal.service.interfacejob;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.entity.InterfaceJobEntity;
import uk.gov.hmcts.opal.repository.InterfaceJobRepository;

@Service
@RequiredArgsConstructor
public class InterfaceJobProcessorService {

    private static final String SYSTEM_POSTED_BY = "interface-jobs";
    private static final String SYSTEM_POSTED_BY_NAME = "interface-jobs";

    private final InterfaceJobRepository interfaceJobRepository;

    @Transactional
    public Optional<Long> processPaymentsInJob(Long interfaceJobId) {
        try {
            InterfaceJobEntity interfaceJob = interfaceJobRepository.findById(interfaceJobId)
                .orElseThrow(() -> new IllegalStateException("Interface job not found with id: " + interfaceJobId));
            return Optional.ofNullable(interfaceJobRepository.processPaymentsInJob(
                interfaceJob.getInterfaceJobId(),
                interfaceJob.getBusinessUnit().getBusinessUnitId(),
                SYSTEM_POSTED_BY,
                SYSTEM_POSTED_BY_NAME));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to process interface job " + interfaceJobId, e);
        }
    }
}
