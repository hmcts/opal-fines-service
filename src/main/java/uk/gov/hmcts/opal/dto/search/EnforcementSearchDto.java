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
public class EnforcementSearchDto implements ToJsonString {

    private String enforcementId;
    private String postedBy;
    private String reason;
    private String warrantReference;
    private String caseReference;
    private String accountType;

}
