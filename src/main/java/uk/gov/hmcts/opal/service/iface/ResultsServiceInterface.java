package uk.gov.hmcts.opal.service.iface;

import java.util.List;
import uk.gov.hmcts.opal.generated.model.GetResultByIdResponseResults;
import uk.gov.hmcts.opal.generated.model.ResultsRefDataResponse;

public interface ResultsServiceInterface {
    GetResultByIdResponseResults getResult(String resultId, boolean includeWelsh);

    ResultsRefDataResponse getResultsByIds(List<String> resultIds, Boolean active, Boolean manualEnforcement,
                                           Boolean generatesHearing, Boolean enforcement, Boolean enforcementOverride);
}
