package uk.gov.hmcts.opal.dto.legacy;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.opal.dto.NoteDto;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LegacySaveNoteResponseDto {

    @JsonProperty("note_id")
    private Long nodeId;

    public NoteDto createClonedAndUpdatedDto(NoteDto noteDto) {
        return noteDto.toBuilder()
            .noteId(nodeId)
            .build();
    }
}
