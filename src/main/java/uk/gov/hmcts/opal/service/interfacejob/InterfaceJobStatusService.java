package uk.gov.hmcts.opal.service.interfacejob;

import java.time.Clock;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.entity.InterfaceJobEntity;
import uk.gov.hmcts.opal.entity.InterfaceJobStatus;
import uk.gov.hmcts.opal.repository.InterfaceJobRepository;

@Service
@RequiredArgsConstructor
public class InterfaceJobStatusService {

    private final InterfaceJobRepository interfaceJobRepository;
    private final Clock clock;

    @Transactional(readOnly = true)
    public boolean isProcessing(Long interfaceJobId) {
        return getJob(interfaceJobId).getStatus() == InterfaceJobStatus.PROCESSING;
    }

    @Transactional
    public void markCompleted(Long interfaceJobId) {
        InterfaceJobEntity interfaceJob = getJob(interfaceJobId);
        interfaceJob.setStatus(InterfaceJobStatus.COMPLETED);
        interfaceJob.setCompletedDateTime(LocalDateTime.now(clock));
    }

    @Transactional
    public void markIgnored(Long interfaceJobId) {
        InterfaceJobEntity interfaceJob = getJob(interfaceJobId);
        interfaceJob.setStatus(InterfaceJobStatus.IGNORED);
        interfaceJob.setCompletedDateTime(LocalDateTime.now(clock));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailed(Long interfaceJobId) {
        InterfaceJobEntity interfaceJob = getJob(interfaceJobId);
        interfaceJob.setStatus(InterfaceJobStatus.FAILED);
        interfaceJob.setCompletedDateTime(LocalDateTime.now(clock));
    }

    private InterfaceJobEntity getJob(Long interfaceJobId) {
        return interfaceJobRepository.findById(interfaceJobId)
            .orElseThrow(() -> new IllegalStateException("Interface job not found with id: " + interfaceJobId));
    }
}
