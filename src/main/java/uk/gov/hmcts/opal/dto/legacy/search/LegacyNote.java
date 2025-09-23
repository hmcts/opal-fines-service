package uk.gov.hmcts.opal.dto.legacy.search;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.opal.dto.RecordType;

@Data
@XmlRootElement(name = "activity_note")
@XmlAccessorType(XmlAccessType.FIELD)
@Builder
public class LegacyNote {

    @NotNull
    @XmlElement(name = "record_type", required = true)
    private RecordType recordType; // uses your existing enum/type

    @NotBlank
    @XmlElement(name = "record_id", required = true)
    private String recordId;

    @NotBlank
    @XmlElement(name = "note_text", required = true)
    private String noteText;

    @NotNull
    @XmlElement(name = "note_type", required = true)
    private String noteType; // uses your existing enum/type

}
