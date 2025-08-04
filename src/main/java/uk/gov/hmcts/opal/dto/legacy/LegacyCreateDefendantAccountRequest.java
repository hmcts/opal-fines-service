package uk.gov.hmcts.opal.dto.legacy;


import jakarta.xml.bind.annotation.XmlElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
public class LegacyCreateDefendantAccountRequest {

    @XmlElement(name = "business_unit_id")
    private Short businessUnitId;

    @XmlElement(name = "business_unit_user_id")
    private String businessUnitUserId;

    @XmlElement(name = "defendant_account")
    private String defendantAccount;
}
