package uk.gov.hmcts.opal.service.report;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.logging.integration.dto.ParticipantIdentifier;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReportMetaData {
    private List<ParticipantIdentifier> pdpoPartyIds;

}
