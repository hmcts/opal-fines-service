package uk.gov.hmcts.opal.service.iface;

import java.math.BigInteger;
import uk.gov.hmcts.opal.dto.MinorCreditorAccountResponse;
import uk.gov.hmcts.opal.generated.model.PatchMinorCreditorAccountRequest;

public interface MinorCreditorAccountServiceInterface {

    MinorCreditorAccountResponse updateMinorCreditorAccount(
        Long minorCreditorAccountId,
        PatchMinorCreditorAccountRequest request,
        BigInteger etag,
        String postedBy);
}
