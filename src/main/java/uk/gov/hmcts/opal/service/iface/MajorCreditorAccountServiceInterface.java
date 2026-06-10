package uk.gov.hmcts.opal.service.iface;

import uk.gov.hmcts.opal.dto.GetMajorCreditorAccountHeaderSummaryResponse;

public interface MajorCreditorAccountServiceInterface {

    GetMajorCreditorAccountHeaderSummaryResponse getHeaderSummary(Long majorCreditorAccountId);
}
