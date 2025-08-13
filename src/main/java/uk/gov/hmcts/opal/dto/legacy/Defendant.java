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
    "defendantAccountId",
    "organisation",
    "organisationName",
    "firstnames",
    "surname"
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Defendant {

    @XmlElement(name = "defendant_account_id")
    private String defendantAccountId;

    @XmlElement(name = "organisation")
    private boolean organisation;

    @XmlElement(name = "organisation_name", nillable = true)
    private String organisationName;

    @XmlElement(name = "firstnames", nillable = true)
    private String firstnames;

    @XmlElement(name = "surname", nillable = true)
    private String surname;
}
