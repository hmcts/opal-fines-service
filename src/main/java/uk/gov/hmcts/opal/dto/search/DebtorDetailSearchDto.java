package uk.gov.hmcts.opal.dto.search;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.opal.dto.ToJsonString;

@Data
@Builder
public class DebtorDetailSearchDto implements ToJsonString {

    private String partyId;
    private String email;
    private String vehicleMake;
    private String vehicleRegistration;
    private String employerName;
    private String employerAddressLine;

}
