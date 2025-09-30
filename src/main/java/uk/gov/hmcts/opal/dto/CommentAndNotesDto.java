package uk.gov.hmcts.opal.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for the "comment_and_notes" object defined in commentsAndNotes.json schema.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.ALWAYS)
public class CommentAndNotesDto implements ToJsonString {

    @JsonProperty("account_comment")
    private String accountComment;

    @JsonProperty("free_text_note_1")
    private String freeTextNote1;

    @JsonProperty("free_text_note_2")
    private String freeTextNote2;

    @JsonProperty("free_text_note_3")
    private String freeTextNote3;
}
