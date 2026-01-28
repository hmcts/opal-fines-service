package uk.gov.hmcts.opal.service.iface;

import uk.gov.hmcts.opal.dto.GetMinorCreditorAccountAtAGlanceResponse;
import uk.gov.hmcts.opal.dto.PostMinorCreditorAccountsSearchResponse;
import uk.gov.hmcts.opal.dto.MinorCreditorSearch;

public interface MinorCreditorServiceInterface {

    PostMinorCreditorAccountsSearchResponse searchMinorCreditors(MinorCreditorSearch minorCreditorSearchDto);

    GetMinorCreditorAccountAtAGlanceResponse getMinorCreditorAtAGlance(String minorCreditorId);
}
