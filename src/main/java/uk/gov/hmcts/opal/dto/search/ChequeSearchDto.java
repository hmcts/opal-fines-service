package uk.gov.hmcts.opal.dto.search;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.opal.dto.ToJsonString;

@Data
@Builder
public class ChequeSearchDto implements ToJsonString {

    private String chequeId;
    private String businessUnitId;

}
