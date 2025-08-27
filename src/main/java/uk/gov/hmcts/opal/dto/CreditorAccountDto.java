package uk.gov.hmcts.opal.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreditorAccountDto {

    @JsonProperty("creditor_account_id")
    private String creditorAccountId;

    @JsonProperty("account_number")
    private String accountNumber;

    @JsonProperty("organisation")
    private boolean organisation;

    @JsonProperty("organisation_name")
    private String organisationName;

    @JsonProperty("firstnames")
    private String firstnames;

    @JsonProperty("surname")
    private String surname;

    @JsonProperty("address_line_1")
    private String addressLine1;

    @JsonProperty("postcode")
    private String postcode;

    @JsonProperty("business_unit_name")
    private String businessUnitName;

    @JsonProperty("business_unit_id")
    private String businessUnitId;

    @JsonProperty("account_balance")
    private double accountBalance;

    @JsonProperty("defendant")
    private DefendantDto defendant;
}
