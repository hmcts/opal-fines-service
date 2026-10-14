package uk.gov.hmcts.opal.dto.legacy;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAccessType;
import java.math.BigInteger;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.common.legacy.model.ErrorResponse;
import uk.gov.hmcts.opal.common.legacy.model.HasErrorResponse;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.gov.hmcts.opal.dto.ToXmlString;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@XmlRootElement(name = "response")
@XmlAccessorType(XmlAccessType.FIELD)
public class LegacyRemoveDefendantAccountEnforcementHoldResponse
    implements ToJsonString, ToXmlString, HasErrorResponse {

    @XmlElement(name = "error_response")
    private ErrorResponse errorResponse;

    @JsonProperty("defendant_account_id")
    @XmlElement(name = "defendant_account_id")
    private Long defendantAccountId;
    @JsonProperty("version")
    @XmlElement(name = "version")
    private BigInteger version;
}
