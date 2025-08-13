package uk.gov.hmcts.opal.dto.legacy.search;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import uk.gov.hmcts.opal.dto.ToXmlString;
import uk.gov.hmcts.opal.dto.legacy.CreditorAccount;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "postMinorCreditorAccountsSearchResponse")
public class LegacyMinorCreditorSearchResultsResponse implements ToXmlString {

    @XmlElement(name = "count", required = true)
    private int count;

    @XmlElementWrapper(name = "creditor_accounts")
    @XmlElement(name = "creditorAccount")
    private List<CreditorAccount> creditorAccounts;
}
