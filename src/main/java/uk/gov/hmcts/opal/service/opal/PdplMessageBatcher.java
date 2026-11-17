package uk.gov.hmcts.opal.service.opal;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import uk.gov.hmcts.opal.exception.PdplLoggingException;
import uk.gov.hmcts.opal.logging.integration.dto.ParticipantIdentifier;
import uk.gov.hmcts.opal.logging.integration.dto.PersonalDataProcessingLogDetails;
import uk.gov.hmcts.opal.logging.integration.messaging.PdpoLogMessage;

/**
 * Partitions PDPO participants using the publisher's JSON envelope and a reserved message-property budget.
 */
@Component
public class PdplMessageBatcher {

    private final ObjectMapper objectMapper;
    private final int payloadBudget;
    private final String logType;

    public PdplMessageBatcher(ObjectMapper objectMapper,
        @Value("${logging-service.pdpl.async.max-message-size-bytes:262144}") int maxMessageSizeBytes,
        @Value("${logging-service.pdpl.async.message-overhead-bytes:65536}") int messageOverheadBytes,
        @Value("${logging-service.pdpl.async.log-type:PDPO}") String logType) {

        if (maxMessageSizeBytes <= 0 || messageOverheadBytes < 0 || messageOverheadBytes >= maxMessageSizeBytes) {
            throw new IllegalArgumentException("PDPO message overhead must fit within the positive transport limit");
        }
        this.objectMapper = objectMapper;
        this.payloadBudget = maxMessageSizeBytes - messageOverheadBytes;
        this.logType = logType;
    }

    public List<PersonalDataProcessingLogDetails> split(PersonalDataProcessingLogDetails details) {
        List<PersonalDataProcessingLogDetails> batches = new ArrayList<>();
        try {
            split(details, details.getIndividuals(), batches);
        } catch (JacksonException exception) {
            throw new PdplLoggingException("Unable to measure the PDPO message payload", exception);
        }
        return List.copyOf(batches);
    }

    private void split(PersonalDataProcessingLogDetails details, List<ParticipantIdentifier> individuals,
        List<PersonalDataProcessingLogDetails> batches) {

        PersonalDataProcessingLogDetails batch = PersonalDataProcessingLogDetails.builder()
            .createdBy(details.getCreatedBy())
            .businessIdentifier(details.getBusinessIdentifier())
            .createdAt(details.getCreatedAt())
            .ipAddress(details.getIpAddress())
            .category(details.getCategory())
            .recipient(details.getRecipient())
            .individuals(List.copyOf(individuals))
            .build();

        if (objectMapper.writeValueAsBytes(new PdpoLogMessage(logType, batch)).length <= payloadBudget) {
            batches.add(batch);
            return;
        }
        if (individuals.size() <= 1) {
            throw new PdplLoggingException("A PDPO message cannot fit within the configured transport limit");
        }

        int middle = individuals.size() / 2;
        split(details, individuals.subList(0, middle), batches);
        split(details, individuals.subList(middle, individuals.size()), batches);
    }
}
