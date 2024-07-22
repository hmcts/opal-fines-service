package uk.gov.hmcts.opal.dto.search;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.opal.dto.ToJsonString;

@Data
@Builder
public class HmrcRequestSearchDto implements ToJsonString {

    private String hmrcRequestId;
    private String businessUnitId;
    private String hmrcRequestNumber;
    private String ownedBy;

}
