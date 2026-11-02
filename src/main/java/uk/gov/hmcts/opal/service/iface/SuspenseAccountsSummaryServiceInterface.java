package uk.gov.hmcts.opal.service.iface;

import uk.gov.hmcts.opal.generated.model.SuspenseItemSummaryCommonStrict;

public interface SuspenseAccountsSummaryServiceInterface {

    SuspenseItemSummaryCommonStrict getSuspenseAccountsSummary(Short businessUnitId, Long suspenseItemNumber);

}
