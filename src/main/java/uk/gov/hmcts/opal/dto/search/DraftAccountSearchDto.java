package uk.gov.hmcts.opal.dto.search;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.gov.hmcts.opal.entity.draft.DraftAccountStatus;
import uk.gov.hmcts.opal.entity.draft.DraftAccountType;

@Data
@Builder
public class DraftAccountSearchDto implements ToJsonString {

    private String draftAccountId;
    private String businessUnitId;
    private DraftAccountType accountType;
    private DraftAccountStatus accountStatus;

}
