package uk.gov.hmcts.opal.dto.search;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.opal.dto.ToJsonString;

@Data
@Builder
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
