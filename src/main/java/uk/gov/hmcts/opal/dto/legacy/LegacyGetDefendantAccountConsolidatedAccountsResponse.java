package uk.gov.hmcts.opal.dto.legacy;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@XmlRootElement(name = "response")
@XmlAccessorType(XmlAccessType.FIELD)
public class LegacyGetDefendantAccountConsolidatedAccountsResponse {

    @JsonProperty("version")
    @XmlElement(name = "version")
    private Long version;

    @JsonProperty("consolidated_accounts")
    @XmlElement(name = "consolidated_accounts")
    private List<LegacyConsolidatedAccount> consolidatedAccounts;
}
