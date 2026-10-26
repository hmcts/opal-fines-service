package uk.gov.hmcts.opal.service.iface;


import uk.gov.hmcts.opal.generated.model.SuspenseItemHistoryCommonStrict;

public interface SuspenseAccountsHistoryServiceInterface {

    SuspenseItemHistoryCommonStrict getSuspenseItemHistory(Long businessUnitId, Long suspenseItemNumber);

}
