package uk.gov.hmcts.opal.dto.legacy;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.dto.AccountSearchDto;
import uk.gov.hmcts.opal.dto.ToJsonString;

import java.util.Optional;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DefendantAccountSearchCriteria implements ToJsonString {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("account_number")
    private String accountNumber;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("business_unit_id")
    private Long businessUnitId;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean organisation;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("organisation_name")
    private String organisationName;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String surname;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String forenames;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String initials;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("birth_date")
    private String birthDate;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("national_insurance_number")
    private String nationalInsuranceNumber;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("prosecutor_case_reference")
    private String prosecutorCaseReference;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("address_line_1")
    private String addressLine1;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean searchAliases;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean liveOnly;

    private Integer firstRowNumber;

    private Integer lastRowNumber;

    public static DefendantAccountSearchCriteria fromAccountSearchDto(AccountSearchDto dto) {
        return DefendantAccountSearchCriteria.builder()
            //.account_number( no account number )
            .surname(dto.getSurname())
            .forenames(dto.getForename())
            .initials(dto.getInitials())
            .birthDate(Optional.ofNullable(dto.getDateOfBirth()).map(d -> d.toLocalDate().toString()).orElse(null))
            .addressLine1(dto.getAddressLineOne())
            .nationalInsuranceNumber(dto.getNiNumber())
            .prosecutorCaseReference(dto.getPcr())
            .businessUnitId(dto.getNumericCourt())
            //.organisation_name(no organisation name)
            //.searchAliases( dunno )
            //.liveOnly( dunno )
            .firstRowNumber(1)
            .lastRowNumber(100)
            .build();
    }
}
