package uk.gov.hmcts.opal.dto.search;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.opal.dto.ToJsonString;

@Data
@Builder
public class ConfigurationItemSearchDto implements ToJsonString {

    private String configurationItemId;
    private String itemName;
    private String businessUnitId;
    private String itemValue;

}
