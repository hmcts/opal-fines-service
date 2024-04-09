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
public class MajorCreditorSearchDto extends AddressSearch  implements ToJsonString {

    private String majorCreditorId;
    private String businessUnitId;
    private String businessUnitName;
    private String majorCreditorCode;

}
