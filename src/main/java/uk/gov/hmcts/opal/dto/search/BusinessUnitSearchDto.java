package uk.gov.hmcts.opal.dto.search;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.dto.ToJsonString;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessUnitSearchDto implements ToJsonString {

    private String businessUnitId;
    private String businessUnitName;
    private String businessUnitCode;
    private String businessUnitType;
    private String accountNumberPrefix;
    private String parentBusinessUnitId;

}
