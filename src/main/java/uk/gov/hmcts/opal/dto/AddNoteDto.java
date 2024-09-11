package uk.gov.hmcts.opal.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
public class AddNoteDto implements ToJsonString {
    @JsonProperty("associated_record_id")
    private String associatedRecordId;
    @JsonProperty("business_unit_id")
    private Short businessUnitId;
    @JsonProperty("note_text")
    private String noteText;
}
