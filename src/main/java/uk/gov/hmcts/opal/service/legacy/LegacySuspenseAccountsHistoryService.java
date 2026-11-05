package uk.gov.hmcts.opal.service.legacy;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.generated.model.SuspenseItemHistoryCommonStrict;
import uk.gov.hmcts.opal.service.iface.SuspenseAccountsHistoryServiceInterface;

@Service
public class LegacySuspenseAccountsHistoryService implements SuspenseAccountsHistoryServiceInterface {

    @Override
    public SuspenseItemHistoryCommonStrict getSuspenseItemHistory(Long businessUnitId, Long suspenseItemNumber) {
        throw new UnsupportedOperationException("LegacySuspenseAccountSummaryService is not implemented yet");
    }

}
