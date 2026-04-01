package uk.gov.hmcts.opal.dto.search;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.opal.dto.ToJsonString;

@Data
@Builder
public class MiscellaneousAccountSearchDto implements ToJsonString {
    private String miscellaneousAccountId;

    private String businessUnitId;
    private String businessUnitName;
    private String businessUnitType;
    private String parentBusinessUnitId;

    private String accountNumber;

    private String partyId;
    private String surname;
    private String forenames;
    private String niNumber;
    private String addressLine;
    private String postcode;
}
