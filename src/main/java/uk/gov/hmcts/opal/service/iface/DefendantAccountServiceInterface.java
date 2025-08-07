package uk.gov.hmcts.opal.service.iface;

import uk.gov.hmcts.opal.dto.DefendantAccountHeaderSummary;

public interface DefendantAccountServiceInterface {
    DefendantAccountHeaderSummary getHeaderSummary(Long defendantAccountId);
}
