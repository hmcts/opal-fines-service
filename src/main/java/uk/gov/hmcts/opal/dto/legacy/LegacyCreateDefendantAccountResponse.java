package uk.gov.hmcts.opal.dto.legacy;


import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.opal.dto.ToXmlString;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
@XmlRootElement(name = "response")
@XmlAccessorType(XmlAccessType.FIELD)
public class LegacyCreateDefendantAccountResponse implements ToXmlString, HasErrorResponse {

    @XmlElement(name = "defendant_account_id")
    private Long defendantAccountId;

    @XmlElement(name = "defendant_account_number")
    private String defendantAccountNumber;

    @XmlElement(name = "error_response")
    private String errorResponse;
}
