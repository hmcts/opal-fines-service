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
import uk.gov.hmcts.opal.dto.Creditor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class LegacyMinorCreditorSearchResultsRequest {

    @JsonProperty("business_unit_ids")
    private List<Integer> businessUnitIds;

    @JsonProperty("active_accounts_only")
    private boolean activeAccountsOnly;

    @JsonProperty("account_number")
    private String accountNumber;

    @JsonProperty("creditor")
    private Creditor creditor;
}
