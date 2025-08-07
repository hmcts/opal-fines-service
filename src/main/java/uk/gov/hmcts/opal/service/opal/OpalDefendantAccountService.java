package uk.gov.hmcts.opal.service.opal;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.DefendantAccountHeaderSummary;
import uk.gov.hmcts.opal.service.iface.DefendantAccountServiceInterface;

@Service
@Slf4j(topic = "opal.OpalDefendantAccountService")
@RequiredArgsConstructor
public class OpalDefendantAccountService implements DefendantAccountServiceInterface {

    public DefendantAccountHeaderSummary getHeaderSummary(Long defendantAccountId) {
        log.debug(":getHeaderSummary: id: {} - NOT YET IMPLEMENTED.", defendantAccountId);
        throw new EntityNotFoundException("Defendant Account not found with id: " + defendantAccountId);
    }

}
