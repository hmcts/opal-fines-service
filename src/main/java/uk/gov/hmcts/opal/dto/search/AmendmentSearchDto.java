package uk.gov.hmcts.opal.dto.search;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.opal.dto.ToJsonString;

@Data
@Builder
public class AmendmentSearchDto implements ToJsonString {
    private String amendmentId;

    private String businessUnitId;
    private String businessUnitName;
    private String businessUnitType;
    private String parentBusinessUnitId;

    private String associatedRecordType;
    private String associatedRecordId;
    private String amendedDate;
    private String amendedBy;
    private String fieldCode;
    private String oldValue;
    private String newValue;
    private String caseReference;
    private String functionCode;
}
