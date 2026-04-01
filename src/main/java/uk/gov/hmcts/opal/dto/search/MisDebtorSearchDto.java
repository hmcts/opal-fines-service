package uk.gov.hmcts.opal.dto.search;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.opal.dto.ToJsonString;

@Data
@Builder
public class MisDebtorSearchDto implements ToJsonString {

    private String misDebtorId;
    private String debtorName;
    private String daysInJail;

}
