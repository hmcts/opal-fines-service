package uk.gov.hmcts.opal.dto.search;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.opal.dto.ToJsonString;

@Data
@Builder
public class SuspenseItemSearchDto implements ToJsonString {

    private String suspenseItemId;
    private String suspenseAccountId;
    private String suspenseItemNumber;
    private String suspenseItemType;
    private String paymentMethod;
    private String courtFeeId;

}
