package uk.gov.hmcts.opal.dto.legacy.search;

import jakarta.validation.constraints.NotNull;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import uk.gov.hmcts.opal.dto.ToXmlString;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "response")
@Builder
public class LegacyAddNoteResponse implements ToXmlString {

    @NotNull
    @XmlElement(name = "version", required = true)
    private Integer version;

    @XmlElement(name = "activity_note", required = true)
    private LegacyNote note;

}
