package uk.gov.hmcts.opal.dto.legacy;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.dto.DefendantAccountSummaryDto;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.gov.hmcts.opal.entity.FullNameBuilder;
import uk.gov.hmcts.opal.util.LocalDateAdapter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class LegacyDefendantAccountSearchResult implements ToJsonString, FullNameBuilder {

    @JsonProperty("defendant_account_id")
    @XmlElement(name = "defendant_account_id")
    private long defendantAccountId;

    @JsonProperty("account_number")
    @XmlElement(name = "account_number")
    private String accountNumber;

    @JsonProperty("organisation")
    @XmlElement(name = "organisation")
    private boolean organisation;

    @JsonProperty("aliases")
    @XmlElementWrapper(name = "aliases")
    @XmlElement(name = "alias")
    private List<LegacyAliasDto> aliases;

    @JsonProperty("address_line_1")
    @XmlElement(name = "address_line_1")
    private String addressLine1;

    @JsonProperty("postcode")
    @XmlElement(name = "postcode")
    private String postcode;

    @JsonProperty("business_unit_name")
    @XmlElement(name = "business_unit_name")
    private String businessUnitName;

    @JsonProperty("business_unit_id")
    @XmlElement(name = "business_unit_id")
    private String businessUnitId;

    @JsonProperty("prosecutor_case_reference")
    @XmlElement(name = "prosecutor_case_reference")
    private String prosecutorCaseReference;

    @JsonProperty("last_enforcement_action")
    @XmlElement(name = "last_enforcement_action")
    private String lastEnforcementAction;

    @JsonProperty("account_balance")
    @XmlElement(name = "account_balance")
    private BigDecimal accountBalance;

    // Organisation-specific
    @JsonProperty("organisation_name")
    @XmlElement(name = "organisation_name")
    private String organisationName;

    // Individual-specific
    @JsonProperty("defendant_title")
    @XmlElement(name = "defendant_title")
    private String title;

    @JsonProperty("defendant_firstnames")
    @XmlElement(name = "defendant_firstnames")
    private String forenames;

    @JsonProperty("defendant_surname")
    @XmlElement(name = "defendant_surname")
    private String surname;

    @JsonProperty("birth_date")
    @XmlElement(name = "birth_date")
    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    private LocalDate birthDate;

    @JsonProperty("national_insurance_number")
    @XmlElement(name = "national_insurance_number")
    private String nationalInsuranceNumber;

    @JsonProperty("parent_guardian_surname")
    @XmlElement(name = "parent_guardian_surname")
    private String parentGuardianSurname;

    @JsonProperty("parent_guardian_firstnames")
    @XmlElement(name = "parent_guardian_firstnames")
    private String parentGuardianFirstnames;

    public DefendantAccountSummaryDto toDefendantAccountSummaryDto() {
        DefendantAccountSummaryDto summary =  DefendantAccountSummaryDto.builder()
            .defendantAccountId(String.valueOf(defendantAccountId))
            .accountNumber(accountNumber)
            .birthDate(birthDate != null ? birthDate.toString() : null)
            .addressLine1(addressLine1)
            .accountBalance(accountBalance != null ? accountBalance.doubleValue() : null)
            .organisation(organisation)
            .aliases(aliases != null
            ? aliases.stream().map(alias -> alias.toAliasDto()).toList()
            : List.of())
            .postcode(postcode)
            .businessUnitName(businessUnitName)
            .businessUnitId(businessUnitId)
            .prosecutorCaseReference(prosecutorCaseReference)
            .lastEnforcementAction(lastEnforcementAction)
            .organisationName(organisation ? organisationName : null)
            .defendantTitle(!organisation ? title : null)
            .defendantFirstnames(!organisation ? forenames : null)
            .defendantSurname(!organisation ? surname : null)
            .nationalInsuranceNumber(!organisation ? nationalInsuranceNumber : null)
            .parentGuardianSurname(!organisation ? parentGuardianSurname : null)
            .parentGuardianFirstnames(!organisation ? parentGuardianFirstnames : null)
            .build();

        return summary;
    }

}
