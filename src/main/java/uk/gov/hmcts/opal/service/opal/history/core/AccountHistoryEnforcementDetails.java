package uk.gov.hmcts.opal.service.opal.history.core;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.dto.CourtReferenceDto;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountHistoryEnforcementDetails implements AccountHistoryDetails {

    private String enforcementAction;

    private Integer daysInDefault;

    private String warrantNumber;

    private LocalDate hearingDate;

    private CourtReferenceDto hearingCourt;

    private String caseNumber;

    private String reason;

    private LocalDate earliestDateOfRelease;
}
