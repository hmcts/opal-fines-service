package uk.gov.hmcts.opal.service.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.opal.service.report.GenericReportService;

@Component
@RequiredArgsConstructor
@Slf4j(topic = "opal.ReportQueueConsumer")
public class ReportQueueConsumerService implements QueueConsumerInterface {

    private final ObjectMapper objectMapper;
    private final GenericReportService genericReportService;

    @Override
    public void consume(String messagePayload) {
        ReportQueueMessage message = parse(messagePayload);
        genericReportService.generateReportInstanceContent(message.instanceId());
        log.info("Report queue message received: {}", message);
    }

    private ReportQueueMessage parse(String messagePayload) {
        if (messagePayload == null || messagePayload.isBlank()) {
            throw new IllegalArgumentException("Report message payload is blank");
        }
        try {
            return objectMapper.readValue(messagePayload, ReportQueueMessage.class);
        } catch (IOException ex) {
            log.error("Report queue message parse failed", ex);
            throw new IllegalArgumentException("Unable to parse Report message payload", ex);
        }
    }
}
