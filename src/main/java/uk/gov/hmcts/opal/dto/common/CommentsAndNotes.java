package uk.gov.hmcts.opal.dto.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.dto.ToJsonString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentsAndNotes implements ToJsonString {

    @JsonProperty("account_comment")
    private String accountNotesAccountComments;

    @JsonProperty("free_text_note_1")
    private String accountNotesFreeTextNote1;

    @JsonProperty("free_text_note_2")
    private String accountNotesFreeTextNote2;

    @JsonProperty("free_text_note_3")
    private String accountNotesFreeTextNote3;
}
