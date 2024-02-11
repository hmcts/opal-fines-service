package uk.gov.hmcts.opal.dto.search;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.opal.dto.ToJsonString;

@Data
@Builder
public class AccountTransferSearchDto implements ToJsonString {

    private String accountTransferId;
    private String businessUnitId;
    private String defendantAccountId;
    private String documentInstanceId;
    private String reason;

}
