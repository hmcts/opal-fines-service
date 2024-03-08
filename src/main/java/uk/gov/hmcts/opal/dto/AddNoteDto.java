package uk.gov.hmcts.opal.dto;


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
    private String associatedRecordId;
    private Short businessUnitId;
    private String noteText;
}
