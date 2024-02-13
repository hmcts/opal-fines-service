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
public class EnforcerSearchDto extends BaseCourtSearch implements ToJsonString {

    private String enforcerId;
    private String enforcerCode;
    private String warrantReferenceSequence;
    private String warrantRegisterSequence;

}
