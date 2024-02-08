package uk.gov.hmcts.opal.dto.search;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.opal.dto.ToJsonString;

@Data
@Builder
public class TillSearchDto implements ToJsonString {

    private String tillId;
    private String businessUnitId;
    private String tillNumber;
    private String ownedBy;

}
