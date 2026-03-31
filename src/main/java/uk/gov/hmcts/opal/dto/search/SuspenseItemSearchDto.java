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
public class SuspenseItemSearchDto implements ToJsonString {

    private String suspenseItemId;
    private String suspenseAccountId;
    private String suspenseItemNumber;
    private String suspenseItemType;
    private String paymentMethod;
    private String courtFeeId;

}
