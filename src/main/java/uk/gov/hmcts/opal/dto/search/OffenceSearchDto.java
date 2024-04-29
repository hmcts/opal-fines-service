package uk.gov.hmcts.opal.dto.search;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.opal.dto.ToJsonString;

@Data
@Builder
public class OffenceSearchDto implements ToJsonString {

    private String offenceId;
    private String cjsCode;
    private String businessUnitId;
    private String businessUnitName;
    private String offenceTitle;
    private String offenceTitleCy;

}
