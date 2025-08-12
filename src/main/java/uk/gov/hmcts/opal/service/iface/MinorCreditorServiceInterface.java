package uk.gov.hmcts.opal.service.iface;

import uk.gov.hmcts.opal.dto.PostMinorCreditorAccountsSearchResponse;
import uk.gov.hmcts.opal.entity.minorcreditor.MinorCreditorSearch;

public interface MinorCreditorServiceInterface {

    PostMinorCreditorAccountsSearchResponse searchMinorCreditors(MinorCreditorSearch minorCreditorSearchDto);

}
