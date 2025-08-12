package uk.gov.hmcts.opal.dto.legacy;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.dto.AccountSummaryDto;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.gov.hmcts.opal.entity.FullNameBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DefendantAccountSearchResult  implements ToJsonString, FullNameBuilder {

    @JsonProperty("defendant_account_id")
    private Long defendantAccountId;

    private String accountNumber;

    @JsonProperty("business_unit_id")
    private Integer businessUnitId;

    @JsonProperty("business_unit_name")
    private String businessUnitName;

    private Boolean organisation;

    @JsonProperty("organisation_name")
    private String organisationName;

    private String title;

    private String surname;

    private String forenames;

    @JsonProperty("birth_date")
    private String birthDate;

    @JsonProperty("address_line_1")
    private String addressLine1;

    @JsonProperty("account_balance")
    private BigDecimal accountBalance;

    private Integer rowNumber;

    public AccountSummaryDto toAccountSummaryDto() {
        return AccountSummaryDto.builder()
            .defendantAccountId(defendantAccountId)
            .accountNo(accountNumber)
            .court(businessUnitName)
            .name(getFullName())
            .dateOfBirth(Optional.ofNullable(birthDate).map(LocalDate::parse).orElse(null)) // Invalid date?
            .addressLine1(addressLine1)
            .balance(accountBalance)
            .build();
    }

}
