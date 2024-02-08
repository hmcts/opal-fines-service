package uk.gov.hmcts.opal.dto.search;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import uk.gov.hmcts.opal.dto.ToJsonString;

@Data
@Builder
@EqualsAndHashCode(callSuper = true)
public class LocalJusticeAreaSearchDto extends AddressSearch implements ToJsonString {

    private String localJusticeAreaId;

}
