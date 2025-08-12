package uk.gov.hmcts.opal.dto.legacy;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {
    "organisationName",
    "defendantFirstnames",
    "defendantSurname"
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Defendant {

    @XmlElement(name = "organisationName")
    private String organisationName;

    @XmlElement(name = "defendantFirstnames")
    private String defendantFirstnames;

    @XmlElement(name = "defendantSurname")
    private String defendantSurname;
}
