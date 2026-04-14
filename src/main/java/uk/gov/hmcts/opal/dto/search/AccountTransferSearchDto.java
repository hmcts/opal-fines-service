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
public class AccountTransferSearchDto implements ToJsonString {

    private String accountTransferId;
    private String businessUnitId;
    private String defendantAccountId;
    private String documentInstanceId;
    private String reason;

}
