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
public class BusinessUnitUserSearchDto implements ToJsonString {

    private String businessUnitUserId;

    private String businessUnitId;
    private String businessUnitName;
    private String businessUnitType;
    private String parentBusinessUnitId;

    private String userId;
    private String username;
    private String userDescription;
}
