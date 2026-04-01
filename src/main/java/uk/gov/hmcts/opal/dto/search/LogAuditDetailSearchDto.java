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
public class LogAuditDetailSearchDto implements ToJsonString {

    private String logAuditDetailId;
    private String userId;
    private String logActionId;
    private String logActionName;
    private String accountNumber;
    private String businessUnitId;
    private String businessUnitName;

}
