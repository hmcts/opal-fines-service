package uk.gov.hmcts.opal.service.opal;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.PostMinorCreditorAccountsSearchResponse;
import uk.gov.hmcts.opal.entity.minorcreditor.MinorCreditorSearch;
import uk.gov.hmcts.opal.service.iface.MinorCreditorServiceInterface;

@Service
@Slf4j(topic = "opal.OpalMinorCreditorService")
@RequiredArgsConstructor
public class OpalMinorCreditorService implements MinorCreditorServiceInterface {

    @Override
    public PostMinorCreditorAccountsSearchResponse searchMinorCreditors(MinorCreditorSearch minorCreditorSearchDto) {
        log.debug(":searchMinorCreditor: id: {} - NOT YET IMPLEMENTED.", 1);
        throw new EntityNotFoundException("Minor Creditor not found with ID: " + 1);
    }

}
