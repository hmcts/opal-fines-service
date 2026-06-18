package uk.gov.hmcts.opal.service.iface;

import uk.gov.hmcts.opal.dto.GetMajorCreditorAccountAtAGlanceResponse;
import uk.gov.hmcts.opal.dto.GetMajorCreditorAccountHeaderSummaryResponse;

public interface MajorCreditorAccountServiceInterface {

    GetMajorCreditorAccountAtAGlanceResponse getAtAGlance(Long majorCreditorAccountId);

    GetMajorCreditorAccountHeaderSummaryResponse getHeaderSummary(Long majorCreditorAccountId);
}
