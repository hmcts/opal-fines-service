package uk.gov.hmcts.opal.dto.search;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.opal.dto.ToJsonString;

@Data
@Builder
public class AmendmentSearchDto implements ToJsonString {
    @JsonProperty("amendment_id")
    private String amendmentId;
    @JsonProperty("business_unit_id")
    private String businessUnitId;
    @JsonProperty("associated_record_type")
    private String associatedRecordType;
    @JsonProperty("associated_record_id")
    private String associatedRecordId;
    @JsonProperty("amended_date")
    private String amendedDate;
    @JsonProperty("amended_by")
    private String amendedBy;
    @JsonProperty("field_code")
    private String fieldCode;
    @JsonProperty("old_value")
    private String oldValue;
    @JsonProperty("new_value")
    private String newValue;
    @JsonProperty("case_reference")
    private String caseReference;
    @JsonProperty("function_code")
    private String functionCode;
}
