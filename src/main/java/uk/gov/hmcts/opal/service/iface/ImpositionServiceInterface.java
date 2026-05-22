package uk.gov.hmcts.opal.service.iface;

import uk.gov.hmcts.opal.dto.GetDefendantAccountImpositionsResponse;

public interface ImpositionServiceInterface {

    GetDefendantAccountImpositionsResponse getImpositions(Long defendantAccountId);
}
