package uk.gov.hmcts.opal.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NotesSearchDto implements ToJsonString {
    private String associatedType;
    private String associatedId;
    private String noteType;
    private String postedBy;
    private DateDto postedDate;  // This is currently a placeholder, and does not work ATM.
}
