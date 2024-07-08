package uk.gov.hmcts.opal.dto.search;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.opal.dto.ToJsonString;

@Data
@Builder
public class DraftAccountSearchDto implements ToJsonString {

    private String draftAccountId;
    private String businessUnitId;
    private String accountType;
    private String accountStatus;

}
