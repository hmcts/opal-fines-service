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
public class AliasSearchDto implements ToJsonString {
    private String aliasId;
    private String party;
    private String surname;
    private String forenames;
    private String niNumber;
    private String addressLine;
    private String postcode;
    private String sequenceNumber;
}
