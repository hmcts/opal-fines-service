package uk.gov.hmcts.opal.service.iface;

import uk.gov.hmcts.opal.generated.model.SuspenseItemSummaryCommonStrict;

public interface SuspenseAccountsSummaryServiceInterface {

    SuspenseItemSummaryCommonStrict getSuspenseAccountsSummary(Long businessUnitId, Long suspenseItemNumber);

}
