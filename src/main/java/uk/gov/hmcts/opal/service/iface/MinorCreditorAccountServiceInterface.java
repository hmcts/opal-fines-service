package uk.gov.hmcts.opal.service.iface;

import uk.gov.hmcts.opal.dto.MinorCreditorAccountResponse;
import uk.gov.hmcts.opal.dto.UpdateMinorCreditorAccountRequest;

public interface MinorCreditorAccountServiceInterface {

    MinorCreditorAccountResponse updateMinorCreditorAccount(Long minorCreditorAccountId,
                                                            UpdateMinorCreditorAccountRequest request,
                                                            String ifMatch,
                                                            String postedBy);
}
