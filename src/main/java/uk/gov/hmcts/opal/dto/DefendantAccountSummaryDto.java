package uk.gov.hmcts.opal.dto;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.ALWAYS;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigInteger;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.dto.search.AliasDto;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(ALWAYS)
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
    private BigDecimal accountBalance;

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

    @JsonProperty("has_collection_order")
    @JsonInclude(Include.NON_NULL)
    private Boolean hasCollectionOrder;

    @JsonProperty("account_version")
    @JsonInclude(Include.NON_NULL)
    private BigInteger accountVersion;

    @JsonProperty("checks")
    @JsonInclude(Include.NON_NULL)
    private Checks checks;

    @Builder
    @Getter
    public static class Checks {

        @JsonProperty("warnings")
        private List<WarnError> warnings;

        @JsonProperty("errors")
        private List<WarnError> errors;
    }

    @Getter
    public static class WarnError {
        public WarnError(String combined) {
            System.out.println(combined);
            String[] split = combined.trim().split("\\|", -1);
            this.reference = split[0]; // Should never be an empty array
            this.message = split.length > 1 ? split[1] : "";
        }

        @JsonProperty("reference")
        private final String reference;

        @JsonProperty("message")
        private final String message;
    }
}
