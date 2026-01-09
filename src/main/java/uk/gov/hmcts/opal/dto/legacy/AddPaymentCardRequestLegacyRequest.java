package uk.gov.hmcts.opal.dto.legacy;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class AddPaymentCardRequestLegacyRequest {

    @XmlElement(name = "defendant_account_id")
    private String defendantAccountId;

    @XmlElement(name = "business_unit_id")
    private String businessUnitId;

    @XmlElement(name = "business_unit_user_id")
    private String businessUnitUserId;

    @XmlElement(name = "version")
    private Integer version;
}
