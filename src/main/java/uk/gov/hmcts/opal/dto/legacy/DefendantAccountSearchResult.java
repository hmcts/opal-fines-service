package uk.gov.hmcts.opal.dto.legacy;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.dto.AccountSummaryDto;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.gov.hmcts.opal.entity.DefendantAccountPartiesEntity;
import uk.gov.hmcts.opal.entity.FullNameBuilder;
import uk.gov.hmcts.opal.entity.PartyEntity;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@XmlRootElement(name = "defendantAccountEntity")
@XmlAccessorType(XmlAccessType.FIELD)

public class DefendantAccountSearchResult implements ToJsonString, FullNameBuilder {

    @XmlElement(name = "defendant_account_id")
    @JsonProperty("defendant_account_id")
    private Long defendantAccountId;

    @XmlElement(name = "account_number")
    private String accountNumber;

    @XmlElement(name = "business_unit_id")
    private Integer businessUnitId;

    @XmlElement(name = "business_unit_name")
    private String businessUnitName;
    @XmlElement(name = "court")
    @JsonProperty("court")
    private String court;

    @XmlElement(name = "organisation")
    private Boolean organisation;

    @XmlElement(name = "organisation_name")
    @JsonProperty("organisation_name")
    private String organisationName;

    @XmlElement(name = "initials")
    private String initials;

    @XmlElement(name = "birth_date")
    @JsonProperty("birth_date")
    private String birthDate;

    @XmlElement(name = "title")
    @JsonProperty("defendant_title")
    private String defendantTitle;

    @XmlElement(name = "forenames")
    @JsonProperty("defendant_firstnames")
    private String defendantFirstnames;


    @XmlElement(name = "surname")
    @JsonProperty("defendant_surname")
    private String defendantSurname;

    @XmlElement(name = "address_line_1")
    @JsonProperty("address_line_1")
    private String addressLine1;

    @XmlElement(name = "account_balance")
    @JsonProperty("account_balance")
    private BigDecimal accountBalance;

    @XmlElement(name = "row_number")
    private Integer rowNumber;

    @XmlElement(name = "prosecutor_case_reference")
    private String prosecutorCaseReference;

    @XmlElement(name = "last_enforcement_action")
    private String lastEnforcementAction;

    @XmlElementWrapper(name = "aliases")
    @XmlElement(name = "alias")
    private List<AliasDto> aliases;

    @XmlElement(name = "national_insurance_number")
    private String nationalInsuranceNumber;

    @XmlElement(name = "parent_guardian_surname")
    private String parentGuardianSurname;

    @XmlElement(name = "parent_guardian_firstnames")
    private String parentGuardianFirstnames;

    @XmlElementWrapper(name = "parties")
    @XmlElement(name = "defendantAccountPartiesEntity")
    private List<DefendantAccountPartiesEntity> parties;


    public AccountSummaryDto toAccountSummaryDto() {
        PartyEntity firstParty = parties != null
            ? parties.stream()
            .filter(p -> "Defendant".equals(p.getAssociationType()))
            .map(DefendantAccountPartiesEntity::getParty)
            .findFirst()
            .orElse(null)
            : null;

        return AccountSummaryDto.builder()
            .defendantAccountId(defendantAccountId != null ? String.valueOf(defendantAccountId) : null)
            .accountNumber(accountNumber)
            .businessUnitName(businessUnitName)
            .organisation(organisation != null ? organisation : false)
            .organisationName(organisationName)
            .aliases(aliases)
            .defendantTitle(firstParty != null ? firstParty.getTitle() : null)
            .defendantFirstnames(firstParty != null ? firstParty.getForenames() : null)
            .defendantSurname(firstParty != null ? firstParty.getSurname() : null)
            .birthDate(firstParty != null ? firstParty.getDateOfBirth() : null)
            .addressLine1(firstParty != null ? firstParty.getAddressLine1() : null)
            .accountBalance(accountBalance)
            .court(court)
            .build();
    }

    @Override
    public String getForenames() {
        return defendantFirstnames;
    }

    @Override
    public String getSurname() {
        return defendantSurname;
    }

    @Override
    public String getTitle() {
        return defendantTitle;
    }

}
