package uk.gov.hmcts.opal.service.messaging;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.repository.TillRepository;
import uk.gov.hmcts.opal.service.report.TillAllocationReportService;

@Service
@RequiredArgsConstructor
public class AllocateTillsQueueProcessingService {

    private final TillRepository tillRepository;
    private final TillAllocationReportService reportService;

    @Transactional
    public void process(TillQueueMessage message) {
        tillRepository.allocateTill(message.tillId(), message.postedBy(), message.postedByName(),
            message.businessUnitId());
        reportService.createReports(message.tillId(), message.requestedBy(), message.postedByName());
    }
}
