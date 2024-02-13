package uk.gov.hmcts.opal.dto.search;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import uk.gov.hmcts.opal.dto.ToJsonString;

@Data
@Builder
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
public class PartySearchDto extends AddressSearch implements ToJsonString {

    private String partyId;
    private String organisationName;
    private String surname;
    private String forenames;
    private String addressLine;
    private String postcode;

}
