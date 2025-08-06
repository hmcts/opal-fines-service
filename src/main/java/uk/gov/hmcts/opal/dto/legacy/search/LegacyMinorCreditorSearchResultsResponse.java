package uk.gov.hmcts.opal.dto.legacy.search;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.opal.entity.AddressEntity;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class LegacyMinorCreditorSearchResultsResponse extends AddressEntity {

    @JsonProperty("creditor_account_id")
    private String creditorAccountId;

    @JsonProperty("account_number")
    private String accountNumber;

    @JsonProperty("business_unit_name")
    private String businessUnitName;

    @JsonProperty("business_unit_id")
    private String businessUnitId;

    @JsonProperty("account_balance")
    private double accountBalance;

    @JsonProperty("organisation")
    private Boolean organisation;

    @JsonProperty("organisation_name")
    private String organisationName;

    @JsonProperty("firstnames")
    private String firstnames;

    @JsonProperty("surname")
    private String surname;

    @JsonProperty("error_response")
    private String errorResponse;
}
