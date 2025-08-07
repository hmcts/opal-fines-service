package uk.gov.hmcts.opal.dto.legacy;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.gov.hmcts.opal.dto.search.AccountSearchDto;
import uk.gov.hmcts.opal.dto.search.DefendantDto;

import java.util.Objects;
import java.util.Optional;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LegacyDefendantAccountSearchCriteria implements ToJsonString {

    @JsonProperty("account_number")
    private String accountNumber;

    @JsonProperty("business_unit_id")
    private Long businessUnitId;

    private Boolean organisation;

    @JsonProperty("organisation_name")
    private String organisationName;

    private String surname;

    private String forenames;

    private String initials;

    @JsonProperty("birth_date")
    private String birthDate;

    @JsonProperty("national_insurance_number")
    private String nationalInsuranceNumber;

    @JsonProperty("prosecutor_case_reference")
    private String prosecutorCaseReference;

    @JsonProperty("address_line_1")
    private String addressLine1;

    @JsonProperty("postcode")
    private String postcode;

    private Boolean searchAliases;

    private Boolean liveOnly;

    private Integer firstRowNumber;

    private Integer lastRowNumber;

    public static LegacyDefendantAccountSearchCriteria fromAccountSearchDto(AccountSearchDto dto) {
        DefendantDto defendant = dto.getDefendant();
        return LegacyDefendantAccountSearchCriteria.builder()
            .accountNumber(null)
            .surname(defendant != null ? defendant.getSurname() : null)
            .forenames(defendant != null ? defendant.getForenames() : null)
            .initials(defendant != null ? defendant.getInitials() : null)
            .birthDate(Optional.ofNullable(defendant)
                .map(DefendantDto::getBirthDate)
                .map(Object::toString)
                .orElse(null))
            .addressLine1(defendant != null ? defendant.getAddressLine1() : null)
            .postcode(defendant != null ? defendant.getPostcode() : null)
            .nationalInsuranceNumber(defendant != null ? defendant.getNationalInsuranceNumber() : null)
            .prosecutorCaseReference(
                Objects.requireNonNull(dto.getReferenceNumberDto(), "ReferenceNumberDto must not be null")
                    .getProsecutorCaseReference()
            )
            .organisation(defendant != null ? defendant.getOrganisation() : null)
            .organisationName(defendant != null ?
                defendant.getOrganisation() ? defendant.getOrganisationName() : null
                : null)
            .searchAliases(false)
            .liveOnly(true)
            .businessUnitId(dto.getBusinessUnitIds() == null ? null : dto.getBusinessUnitIds().get(0).longValue())
            .firstRowNumber(1)
            .lastRowNumber(100)
            .build();
    }

}
