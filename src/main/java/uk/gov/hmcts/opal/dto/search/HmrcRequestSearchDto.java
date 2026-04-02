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
public class HmrcRequestSearchDto implements ToJsonString {

    private String hmrcRequestId;
    private String businessUnitId;
    private String hmrcRequestNumber;
    private String ownedBy;

}
