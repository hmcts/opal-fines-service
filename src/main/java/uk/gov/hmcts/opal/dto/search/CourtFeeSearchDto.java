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
public class CourtFeeSearchDto implements ToJsonString {

    private String courtFeeId;
    private String businessUnitId;
    private String businessUnitName;
    private String courtFeeCode;
    private String description;
    private String statsCode;

}
