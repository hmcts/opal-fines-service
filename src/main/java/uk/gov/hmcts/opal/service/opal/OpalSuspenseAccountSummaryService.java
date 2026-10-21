package uk.gov.hmcts.opal.service.opal;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.generated.model.SuspenseItemSummaryCommonStrict;
import uk.gov.hmcts.opal.service.iface.SuspenseAccountsSummaryServiceInterface;

@Service
public class OpalSuspenseAccountSummaryService implements SuspenseAccountsSummaryServiceInterface {

    @Override
    public SuspenseItemSummaryCommonStrict getSuspenseAccountsSummary(Long businessUnitId, Long suspenseItemNumber) {
        throw new UnsupportedOperationException("LegacySuspenseAccountSummaryService is not implemented yet");
    }

}
