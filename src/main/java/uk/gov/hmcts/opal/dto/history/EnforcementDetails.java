package uk.gov.hmcts.opal.dto.history;

import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EnforcementDetails implements DefendantAccountHistoryDetails {

    private String enforcementAction;

    private Integer daysInDefault;

    private String warrantNumber;

    private LocalDate hearingDate;

    private CourtReferenceDto hearingCourt;

    private String caseNumber;

    private String reason;

    private LocalDate earliestDateOfRelease;
}
