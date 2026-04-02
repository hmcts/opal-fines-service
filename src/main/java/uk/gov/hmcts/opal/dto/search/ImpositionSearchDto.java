package uk.gov.hmcts.opal.dto.search;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.dto.ToJsonString;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ImpositionSearchDto implements ToJsonString {

    private String impositionId;
    private String defendantAccountId;
    private String postedBy;
    private String postedByUserId;
    private String resultId;
    private String imposingCourtId;
    private String offenceId;
    private String creditorAccountId;
    private String unitFineUnits;

}
