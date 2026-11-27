package uk.gov.hmcts.opal.service.messaging;

import static org.mockito.Mockito.inOrder;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.repository.TillRepository;
import uk.gov.hmcts.opal.service.report.TillAllocationReportService;

@ExtendWith(MockitoExtension.class)
class AllocateTillsQueueProcessingServiceTest {

    @Mock
    private TillRepository tillRepository;
    @Mock
    private TillAllocationReportService reportService;
    @InjectMocks
    private AllocateTillsQueueProcessingService service;

    @Test
    void createsReportsAfterAllocation() {
        TillQueueMessage message = new TillQueueMessage(123L, (short) 78, "USER01", "Opal User", 42L);

        service.process(message);

        InOrder order = inOrder(tillRepository, reportService);
        order.verify(tillRepository).allocateTill(123L, "USER01", "Opal User", (short) 78);
        order.verify(reportService).createReports(123L, 42L, "Opal User");
    }
}
