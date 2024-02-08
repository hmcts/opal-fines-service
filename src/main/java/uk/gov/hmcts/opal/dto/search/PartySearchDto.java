package uk.gov.hmcts.opal.dto.search;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.opal.dto.ToJsonString;

@Data
@Builder
public class PartySearchDto implements ToJsonString {

    private String partyId;
    private String organisationName;
    private String surname;
    private String forenames;
    private String addressLine;
    private String postcode;

}
