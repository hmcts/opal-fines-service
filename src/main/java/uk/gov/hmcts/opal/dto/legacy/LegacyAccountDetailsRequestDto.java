package uk.gov.hmcts.opal.dto.legacy;

import jakarta.xml.bind.annotation.XmlElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.opal.dto.ToJsonString;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
public class LegacyAccountDetailsRequestDto implements ToJsonString {

    @XmlElement(name = "defendant_account_id")
    private Long defendantAccountId;

}
