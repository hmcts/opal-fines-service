package uk.gov.hmcts.opal.dto.search;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.opal.dto.ToJsonString;

@Data
@Builder
public class LogAuditDetailSearchDto implements ToJsonString {

    private String logAuditDetailId;
    private String userId;
    private String logActionId;
    private String logActionName;
    private String accountNumber;
    private String businessUnitId;
    private String businessUnitName;

}
