package uk.gov.hmcts.opal.dto.search;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.opal.dto.ToJsonString;

@Data
@Builder
public class SuspenseAccountSearchDto implements ToJsonString {

    private String suspenseAccountId;
    private String businessUnitId;
    private String businessUnitName;
    private String accountNumber;

}
