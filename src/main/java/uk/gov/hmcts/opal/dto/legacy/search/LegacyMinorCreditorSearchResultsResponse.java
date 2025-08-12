package uk.gov.hmcts.opal.dto.legacy.search;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.dto.ToXmlString;
import uk.gov.hmcts.opal.dto.legacy.CreditorAccount;

import java.util.List;

@XmlRootElement(name = "PostMinorCreditorAccountsSearchLegacyResponse")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = { "count", "creditorAccounts" })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LegacyMinorCreditorSearchResultsResponse implements ToXmlString {

    @XmlElement(name = "count", required = true)
    private int count;

    @XmlElementWrapper(name = "creditorAccounts")
    @XmlElement(name = "creditorAccount")
    private List<CreditorAccount> creditorAccounts;
}
