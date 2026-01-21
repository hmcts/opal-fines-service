package uk.gov.hmcts.opal.service.iface;

import uk.gov.hmcts.opal.dto.GetMinorCreditorAccountHeaderSummaryResponse;
import uk.gov.hmcts.opal.dto.PostMinorCreditorAccountsSearchResponse;
import uk.gov.hmcts.opal.dto.MinorCreditorSearch;

public interface MinorCreditorServiceInterface {

    PostMinorCreditorAccountsSearchResponse searchMinorCreditors(MinorCreditorSearch minorCreditorSearchDto);

    GetMinorCreditorAccountHeaderSummaryResponse getHeaderSummary(
        Long minorCreditorAccountId
    );
}
