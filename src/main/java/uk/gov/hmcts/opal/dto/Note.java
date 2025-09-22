package uk.gov.hmcts.opal.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Note implements ToJsonString {

    @JsonProperty("record_type")
    private RecordType recordType;

    @JsonProperty("record_id")
    private String recordId;

    @JsonProperty("note_text")
    private String noteText;

    @JsonProperty("note_type")
    private String noteType; // Always "AA"
}
