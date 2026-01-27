package uk.gov.hmcts.opal.dto.legacy;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.dto.DefendantDto;

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
public class LegacyDefendant {

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

    @XmlElement(name = "account_number")
    private String accountNumber;

    @XmlElement(name = "account_id")
    private Long accountId;

    public DefendantDto toOpalDto() {
        return DefendantDto.builder()
            .defendantAccountId(this.getDefendantAccountId())
            .organisation(this.isOrganisation())
            .organisationName(this.getOrganisationName())
            .firstnames(this.getFirstnames())
            .surname(this.getSurname())
            .accountNumber(this.getAccountNumber())
            .accountId(this.getAccountId())
            .build();
    }
}
