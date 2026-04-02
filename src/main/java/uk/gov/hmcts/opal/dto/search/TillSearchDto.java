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
public class TillSearchDto implements ToJsonString {

    private String tillId;
    private String businessUnitId;
    private String tillNumber;
    private String ownedBy;

}
