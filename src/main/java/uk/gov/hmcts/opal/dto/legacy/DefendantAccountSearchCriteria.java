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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DefendantAccountSearchCriteria implements ToJsonString {

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

    private Boolean searchAliases;

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
            .businessUnitId(dto.getNumericCourt().orElse(null))
            //.organisation_name(no organisation name)
            //.searchAliases( dunno )
            //.liveOnly( dunno )
            .firstRowNumber(1)
            .lastRowNumber(100)
            .build();
    }
}
