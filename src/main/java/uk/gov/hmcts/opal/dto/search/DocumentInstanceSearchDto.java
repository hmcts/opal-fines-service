package uk.gov.hmcts.opal.dto.search;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.opal.dto.ToJsonString;

@Data
@Builder
public class DocumentInstanceSearchDto implements ToJsonString {

    private String documentInstanceId;
    private String documentId;
    private String businessUnitId;
    private String generatedBy;
    private String content;

}
