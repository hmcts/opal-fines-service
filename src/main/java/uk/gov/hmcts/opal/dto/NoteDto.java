package uk.gov.hmcts.opal.dto;


import com.fasterxml.jackson.annotation.JsonFormat;
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
public class NoteDto implements ToJsonString {

    private Long noteId;

    private String noteType;

    private String associatedRecordType;

    private String associatedRecordId;

    private Short businessUnitId;

    private String noteText;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime postedDate;

    private String postedBy;

    private Long postedByUserId;

}
