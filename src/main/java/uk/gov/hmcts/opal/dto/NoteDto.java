package uk.gov.hmcts.opal.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

import java.time.LocalDateTime;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
public class NoteDto {
    private Long noteId;
    private String noteType;
    private String associatedRecordType;
    private String associatedRecordId;
    private String noteText;
    private LocalDateTime postedDate;
    private String postedBy;

}
