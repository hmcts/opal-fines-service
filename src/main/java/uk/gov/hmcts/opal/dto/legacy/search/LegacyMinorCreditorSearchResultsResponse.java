package uk.gov.hmcts.opal.dto.legacy.search;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlElements;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import uk.gov.hmcts.opal.common.legacy.model.ErrorResponse;
import uk.gov.hmcts.opal.common.legacy.model.HasErrorResponse;
import uk.gov.hmcts.opal.dto.ToXmlString;
import uk.gov.hmcts.opal.dto.legacy.CreditorAccount;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "response")
@Builder
public class LegacyMinorCreditorSearchResultsResponse implements ToXmlString, HasErrorResponse {

    @XmlElement(name = "error_response")
    private ErrorResponse errorResponse;

    @XmlElement(name = "count", required = true)
    private int count;

    @XmlElementWrapper(name = "creditor_accounts")
    @XmlElements({
        @XmlElement(name = "creditorAccount", type = CreditorAccount.class),
        @XmlElement(name = "creditor_accounts_element", type = CreditorAccount.class)
    })
    private List<CreditorAccount> creditorAccounts;
}
