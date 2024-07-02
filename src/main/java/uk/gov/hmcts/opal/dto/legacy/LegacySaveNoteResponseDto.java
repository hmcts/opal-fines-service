package uk.gov.hmcts.opal.dto.legacy;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
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
@XmlRootElement(name = "LegacySaveNoteResponse")
@XmlAccessorType(XmlAccessType.FIELD)
public class LegacySaveNoteResponseDto {

    @JsonProperty("note_id")
    private Long noteId;

    public NoteDto createClonedAndUpdatedDto(NoteDto noteDto) {
        return noteDto.toBuilder()
            .noteId(noteId)
            .build();
    }
}
