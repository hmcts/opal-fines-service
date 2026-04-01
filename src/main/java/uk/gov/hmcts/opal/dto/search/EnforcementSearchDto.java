package uk.gov.hmcts.opal.dto.search;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.opal.dto.ToJsonString;

@Data
@Builder
public class EnforcementSearchDto implements ToJsonString {

    private String enforcementId;
    private String postedBy;
    private String reason;
    private String warrantReference;
    private String caseReference;
    private String accountType;

}
