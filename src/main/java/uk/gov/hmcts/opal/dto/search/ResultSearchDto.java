package uk.gov.hmcts.opal.dto.search;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.opal.dto.ToJsonString;

@Data
@Builder
public class ResultSearchDto implements ToJsonString {

    private String resultId;
    private String resultTitle;
    private String resultTitleCy;
    private String resultType;
    private String impositionCategory;
    private String impositionAllocationPriority;
    private String impositionCreditor;
    private String userEntries;

}
