package uk.gov.hmcts.opal.service.opal;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.generated.model.SuspenseItemHistoryCommonStrict;
import uk.gov.hmcts.opal.service.iface.SuspenseAccountsHistoryServiceInterface;

@Service
public class OpalSuspenseAccountsHistoryService implements SuspenseAccountsHistoryServiceInterface {

    @Override
    public SuspenseItemHistoryCommonStrict getSuspenseItemHistory(Long businessUnitId, Long suspenseItemNumber) {
        throw new UnsupportedOperationException("OpalSuspenseAccountsHistoryService is not implemented yet");
    }

}
