package uk.gov.hmcts.opal.dto.search;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.opal.dto.ToJsonString;

@Data
@Builder
public class CommittalWarrantProgressSearchDto implements ToJsonString {

    private String defendantAccountId;
    private String enforcementId;
    private String prisonId;

}
