package uk.gov.hmcts.opal.service.report;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.PdplIdentifierType;
import uk.gov.hmcts.opal.logging.integration.dto.ParticipantIdentifier;

@Getter
@Service
public class ReportMetadataContext {

    private final List<ParticipantIdentifier> participants = new ArrayList<>();

    public void addParticipant(String identifier, PdplIdentifierType type) {
        if (identifier == null) {
            return;
        }
        ParticipantIdentifier newParticipant = new ParticipantIdentifier(identifier, type);
        if (!participants.contains(newParticipant)) {
            participants.add(newParticipant);
        }
    }

}
