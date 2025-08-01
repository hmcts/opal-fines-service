package uk.gov.hmcts.opal.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.dto.search.AliasDto;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DefendantAccountSummaryDto implements ToJsonString {

    @JsonProperty("defendant_account_id")
    private String defendantAccountId;

    @JsonProperty("account_number")
    private String accountNumber;

    @JsonProperty("organisation")
    private Boolean organisation;

    @JsonProperty("aliases")
    private List<AliasDto> aliases;

    @JsonProperty("address_line_1")
    private String addressLine1;

    @JsonProperty("postcode")
    private String postcode;

    @JsonProperty("business_unit_name")
    private String businessUnitName;

    @JsonProperty("business_unit_id")
    private String businessUnitId;

    @JsonProperty("prosecutor_case_reference")
    private String prosecutorCaseReference;

    @JsonProperty("last_enforcement_action")
    private String lastEnforcementAction;

    @JsonProperty("account_balance")
    private Double accountBalance;

    @JsonProperty("organisation_name")
    private String organisationName;

    @JsonProperty("defendant_title")
    private String defendantTitle;

    @JsonProperty("defendant_firstnames")
    private String defendantFirstnames;

    @JsonProperty("defendant_surname")
    private String defendantSurname;

    @JsonProperty("birth_date")
    private String birthDate; // Use ISO string like "1980-02-03"

    @JsonProperty("national_insurance_number")
    private String nationalInsuranceNumber;

    @JsonProperty("parent_guardian_surname")
    private String parentGuardianSurname;

    @JsonProperty("parent_guardian_firstnames")
    private String parentGuardianFirstnames;
}
