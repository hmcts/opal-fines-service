package uk.gov.hmcts.opal.service.report;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import uk.gov.hmcts.opal.logging.integration.dto.ParticipantIdentifier;

@Data
@AllArgsConstructor
public class ReportMetaData {
    private List<ParticipantIdentifier> pdpoPartyIds;

}
