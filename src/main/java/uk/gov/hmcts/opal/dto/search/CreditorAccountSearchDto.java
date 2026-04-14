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
public class CreditorAccountSearchDto implements ToJsonString {

    private String creditorAccountId;
    private String businessUnitId;
    private String businessUnitName;
    private String accountsNumber;
    private String creditorAccountType;
    private String majorCreditorId;
    private String minorCreditorPartyId;
    private String bankSortCode;
    private String bankAccountNumber;
    private String bankAccountName;
    private String bankAccountReference;
    private String bankAccountType;

}
