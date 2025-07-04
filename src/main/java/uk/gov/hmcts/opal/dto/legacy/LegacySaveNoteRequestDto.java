package uk.gov.hmcts.opal.dto.legacy;


import com.fasterxml.jackson.annotation.JsonFormat;
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
public class LegacySaveNoteRequestDto {

    @JsonProperty("associated_record_id")
    private String associatedRecordId;

    @JsonProperty("associated_record_type")
    private String associatedRecordType;

    @JsonProperty("note_text")
    private String noteText;

    @JsonProperty("posted_by")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private String getBusinessUnitUserId;

    public static LegacySaveNoteRequestDto fromNoteDto(NoteDto dto) {
        return LegacySaveNoteRequestDto.builder()
            .associatedRecordId(dto.getAssociatedRecordId())
            .associatedRecordType(dto.getAssociatedRecordType())
            .noteText(dto.getNoteText())
            .getBusinessUnitUserId(dto.getBusinessUnitUserId())
            .build();

    }
}
