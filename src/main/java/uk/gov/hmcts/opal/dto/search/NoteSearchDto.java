package uk.gov.hmcts.opal.dto.search;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.opal.dto.DateDto;
import uk.gov.hmcts.opal.dto.ToJsonString;

@Data
@Builder
public class NoteSearchDto implements ToJsonString {
    private String associatedType;
    private String associatedId;
    private String noteType;
    private String postedBy;
    private DateDto postedDate;  // This is currently a placeholder, and does not work ATM.
}
