package uk.gov.hmcts.opal.service.messaging;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.service.interfacejob.InterfaceJobFailurePersistenceService;
import uk.gov.hmcts.opal.service.interfacejob.InterfaceJobProcessorService;
import uk.gov.hmcts.opal.service.interfacejob.InterfaceJobStatusService;
import uk.gov.hmcts.opal.service.report.PreAllocatedCashTillService;

@Service
@Slf4j(topic = "opal.InterfaceJobQueueConsumer")
@RequiredArgsConstructor
public class InterfaceJobQueueProcessingService {

    private static final Long SYSTEM_REQUESTED_BY = 0L;
    private static final String SYSTEM_REQUESTED_BY_NAME = "interface-jobs";

    private final InterfaceJobProcessorService interfaceJobProcessorService;
    private final InterfaceJobStatusService interfaceJobStatusService;
    private final InterfaceJobFailurePersistenceService interfaceJobFailurePersistenceService;
    private final PreAllocatedCashTillService preAllocatedCashTillService;

    @Transactional
    public void processProcessingJob(Long interfaceJobId) {
        Optional<Long> tillId = interfaceJobProcessorService.processPaymentsInJob(interfaceJobId);
        if (tillId.isPresent()) {
            Long reportInstanceId = preAllocatedCashTillService.createPreAllocatedReportInstance(
                tillId.get(), SYSTEM_REQUESTED_BY, SYSTEM_REQUESTED_BY_NAME);
            interfaceJobStatusService.markCompleted(interfaceJobId);
            log.info("Interface job {} completed and created report instance {}", interfaceJobId, reportInstanceId);
        } else {
            interfaceJobStatusService.markIgnored(interfaceJobId);
            log.info("Interface job {} completed with no till created", interfaceJobId);
        }
    }

    public void handleProcessingFailure(Long interfaceJobId, RuntimeException ex) {
        interfaceJobStatusService.markFailed(interfaceJobId);
        interfaceJobFailurePersistenceService.insertFailureMessage(interfaceJobId, ex);
    }
}
