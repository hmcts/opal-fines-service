package uk.gov.hmcts.opal.dto.search;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.dto.ToJsonString;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentInSearchDto implements ToJsonString {

    private String paymentInId;
    private String paymentMethod;
    private String destinationType;
    private String allocationType;
    private String associatedRecordType;
    private String thirdPartyPayerName;
    private String additionalInformation;

}
