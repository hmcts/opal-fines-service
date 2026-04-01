package uk.gov.hmcts.opal.dto.search;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.opal.dto.ToJsonString;

@Data
@Builder
public class LogActionSearchDto implements ToJsonString {

    private String logActionId;
    private String logActionName;

}
