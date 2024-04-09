package uk.gov.hmcts.opal.dto.search;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.opal.dto.ToJsonString;

@Data
@Builder
public class OffenseSearchDto implements ToJsonString {

    private String offenseId;
    private String cjsCode;
    private String businessUnitId;
    private String businessUnitName;
    private String offenceTitle;
    private String offenceTitleCy;

}
