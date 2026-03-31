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
public class ConfigurationItemSearchDto implements ToJsonString {

    private String configurationItemId;
    private String itemName;
    private String businessUnitId;
    private String itemValue;

}
