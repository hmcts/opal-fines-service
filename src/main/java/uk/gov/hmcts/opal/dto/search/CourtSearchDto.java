package uk.gov.hmcts.opal.dto.search;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import uk.gov.hmcts.opal.dto.ToJsonString;

@Data
@Builder
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CourtSearchDto extends AddressCySearch implements ToJsonString {

    private String courtId;
    private String businessUnitId;
    private String courtCode;
    private String parentCourtId;
    private String localJusticeAreaId;
    private String nationalCourtCode;

}
