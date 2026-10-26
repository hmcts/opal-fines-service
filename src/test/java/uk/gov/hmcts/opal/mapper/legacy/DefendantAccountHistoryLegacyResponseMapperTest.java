package uk.gov.hmcts.opal.mapper.legacy;

import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import uk.gov.hmcts.opal.dto.history.PaymentTermsDetails;
import uk.gov.hmcts.opal.dto.legacy.GetDefendantAccountHistoryLegacyResponse;
import uk.gov.hmcts.opal.dto.legacy.LegacyInstalmentPeriod;
import uk.gov.hmcts.opal.dto.legacy.LegacyPaymentTermsType;

class DefendantAccountHistoryLegacyResponseMapperTest {

    private final DefendantAccountHistoryLegacyResponseMapper mapper =
        Mappers.getMapper(DefendantAccountHistoryLegacyResponseMapper.class);

    @Test
    void mapDetails_whenInstalmentPeriodCodeIsMissing_omitsInstalmentPeriod() {
        GetDefendantAccountHistoryLegacyResponse.LegacyDefendantAccountHistoryDetails details =
            GetDefendantAccountHistoryLegacyResponse.LegacyDefendantAccountHistoryDetails.builder()
                .paymentTermsType(new LegacyPaymentTermsType(LegacyPaymentTermsType.PaymentTermsTypeCode.I))
                .instalmentPeriod(new LegacyInstalmentPeriod())
                .build();

        PaymentTermsDetails mapped = (PaymentTermsDetails) mapper.mapDetails("Payment terms", details);

        assertNull(mapped.getInstalmentPeriod());
    }
}
