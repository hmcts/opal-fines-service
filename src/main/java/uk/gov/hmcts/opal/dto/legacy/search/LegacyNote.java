package uk.gov.hmcts.opal.dto.legacy.search;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.dto.RecordType;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
@NoArgsConstructor          // <-- JAXB needs this
@AllArgsConstructor
@Builder
public class LegacyNote {

    @JsonProperty("record_type")
    @XmlElement(name = "record_type", required = true)
    private RecordType recordType; // uses your existing enum/type

    @NotBlank
    @JsonProperty("record_id")
    @XmlElement(name = "record_id", required = true)
    private String recordId;

    @NotBlank
    @JsonProperty("note_text")
    @XmlElement(name = "note_text", required = true)
    private String noteText;

    @NotBlank
    @Pattern(regexp = "AA", message = "note_type must be 'AA'")
    @JsonProperty("note_type")
    @XmlElement(name = "note_type", required = true)
    private String noteType; // uses your existing enum/type

}
