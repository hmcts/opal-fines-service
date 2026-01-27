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
    "accountBalance",
    "defendant"
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreditorAccount {

    @XmlElement(name = "creditor_account_id")
    private String creditorAccountId;

    @XmlElement(name = "account_number")
    private String accountNumber;

    @XmlElement(name = "organisation")
    private boolean organisation;

    @XmlElement(name = "organisation_name", nillable = true)
    private String organisationName;

    @XmlElement(name = "firstnames", nillable = true)
    private String firstnames;

    @XmlElement(name = "surname", nillable = true)
    private String surname;

    @XmlElement(name = "address_line_1")
    private String addressLine1;

    @XmlElement(name = "postcode", nillable = true)
    private String postcode;

    @XmlElement(name = "business_unit_name")
    private String businessUnitName;

    @XmlElement(name = "business_unit_id")
    private String businessUnitId;

    @XmlElement(name = "account_balance")
    private double accountBalance;

    @XmlElement(name = "defendant", required = true)
    private LegacyDefendant defendant;
}
