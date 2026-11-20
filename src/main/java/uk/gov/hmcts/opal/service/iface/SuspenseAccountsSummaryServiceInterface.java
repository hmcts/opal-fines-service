package uk.gov.hmcts.opal.service.iface;

import java.math.BigInteger;
import uk.gov.hmcts.opal.dto.VersionedResponse;
import uk.gov.hmcts.opal.generated.model.SuspenseItemSummaryCommonStrict;

public interface SuspenseAccountsSummaryServiceInterface {

    VersionedResponse<SuspenseItemSummaryCommonStrict> getSuspenseAccountsSummary(Short businessUnitId,
        BigInteger suspenseItemNumber);

}
