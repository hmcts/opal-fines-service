package uk.gov.hmcts.opal.service.legacy;

import java.math.BigInteger;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.generated.model.SuspenseItemSummaryCommonStrict;
import uk.gov.hmcts.opal.service.iface.SuspenseAccountsSummaryServiceInterface;

@Service
public class LegacySuspenseAccountSummaryService implements SuspenseAccountsSummaryServiceInterface {

    @Override
    public SuspenseItemSummaryCommonStrict getSuspenseAccountsSummary(Short businessUnitId,
        BigInteger suspenseItemNumber) {
        throw new UnsupportedOperationException("LegacySuspenseAccountSummaryService is not implemented yet");
    }
}
