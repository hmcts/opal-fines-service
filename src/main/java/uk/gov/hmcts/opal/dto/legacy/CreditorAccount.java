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
    "creditorAccountId",
    "accountNumber",
    "organisation",
    "organisationName",
    "firstnames",
    "surname",
    "addressLine1",
    "postcode",
    "businessUnitName",
    "businessUnitId",
    "defendantAccountId",
    "accountBalance",
    "defendant"
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreditorAccount {

    @XmlElement(name = "creditorAccountId", required = true)
    private String creditorAccountId;

    @XmlElement(name = "accountNumber", required = true)
    private String accountNumber;

    @XmlElement(name = "organisation", required = true)
    private boolean organisation;

    @XmlElement(name = "organisationName")
    private String organisationName;

    @XmlElement(name = "firstnames")
    private String firstnames;

    @XmlElement(name = "surname")
    private String surname;

    @XmlElement(name = "addressLine1", required = true)
    private String addressLine1;

    @XmlElement(name = "postcode")
    private String postcode;

    @XmlElement(name = "businessUnitName", required = true)
    private String businessUnitName;

    @XmlElement(name = "businessUnitId", required = true)
    private String businessUnitId;

    @XmlElement(name = "defendantAccountId", required = true)
    private String defendantAccountId;

    @XmlElement(name = "accountBalance", required = true)
    private double accountBalance;

    @XmlElement(name = "defendant", required = true)
    private Defendant defendant;
}
