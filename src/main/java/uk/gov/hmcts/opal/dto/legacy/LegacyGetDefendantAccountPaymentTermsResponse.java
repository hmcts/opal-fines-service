package uk.gov.hmcts.opal.dto.legacy;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.dto.ToXmlString;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LegacyGetDefendantAccountPaymentTermsResponse implements ToXmlString {

    private Integer version;

    private LegacyPaymentTerms paymentTerms;

    private LegacyPostedDetails postedDetails;

    private LocalDate paymentCardLastRequested;

    private LocalDate dateLastAmended;

    private Boolean extension;

    private String lastEnforcement;
}
