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
public class DraftAccountSearchDto implements ToJsonString {

    private String draftAccountId;
    private String businessUnitId;
    private String accountType;
    private String accountStatus;

}
