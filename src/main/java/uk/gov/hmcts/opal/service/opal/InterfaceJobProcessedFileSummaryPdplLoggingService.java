package uk.gov.hmcts.opal.service.opal;

import java.time.Clock;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserStateV2;
import uk.gov.hmcts.opal.dto.PdplIdentifierType;
import uk.gov.hmcts.opal.logging.integration.dto.ParticipantIdentifier;
import uk.gov.hmcts.opal.logging.integration.dto.PersonalDataProcessingCategory;
import uk.gov.hmcts.opal.logging.integration.service.LoggingService;

@Service
@Slf4j(topic = "opal.pdplLoggingService")
public class InterfaceJobProcessedFileSummaryPdplLoggingService extends AbstractPdplLoggingService {

    private static final String BUSINESS_IDENTIFIER = "View File Processing Summary";

    public InterfaceJobProcessedFileSummaryPdplLoggingService(LoggingService loggingService, Clock clock) {
        super(loggingService, clock);
    }

    public void logView(UserStateV2 userState) {
        ParticipantIdentifier payer = ParticipantIdentifier.builder()
            .type(PdplIdentifierType.PAYER)
            .build();

        try {
            // No recipient is involved when viewing a file processing summary.
            boolean sent = logPdpl(BUSINESS_IDENTIFIER, PersonalDataProcessingCategory.CONSULTATION,
                List.of(payer), null, userState.getUserId());
            if (!sent) {
                log.error("Unable to submit PDPO log for {}", BUSINESS_IDENTIFIER);
            }
        } catch (RuntimeException e) {
            log.error("Unable to submit PDPO log for {}", BUSINESS_IDENTIFIER, e);
        }
    }
}
