package uk.gov.hmcts.opal.dto;


import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NoteDto {
    private Long noteId;
    private String noteType;
    private String associatedRecordType;
    private String associatedRecordId;
    private String noteText;
    private LocalDateTime postedDate;
    private String postedBy;

}
