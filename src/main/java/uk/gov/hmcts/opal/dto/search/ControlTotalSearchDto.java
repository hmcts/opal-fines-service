package uk.gov.hmcts.opal.dto.search;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.opal.dto.ToJsonString;

@Data
@Builder
public class ControlTotalSearchDto implements ToJsonString {

    private String controlTotalId;
    private String businessUnitId;

}
