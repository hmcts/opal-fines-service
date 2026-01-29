package uk.gov.hmcts.opal.dto.legacy;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@XmlRootElement(name = "request")
@XmlAccessorType(XmlAccessType.FIELD)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class  LegacyGetMinorCreditorAccountAtAGlanceRequest {

    @XmlElement(name = "creditor_account_id", required = true)
    private String creditorAccountId;

    public static LegacyGetMinorCreditorAccountAtAGlanceRequest createRequest(String minorCreditorId) {
        return null;
    }
}
