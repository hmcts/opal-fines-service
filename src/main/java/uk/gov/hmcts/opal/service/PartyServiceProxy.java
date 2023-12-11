package uk.gov.hmcts.opal.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.PartyDto;

@Service
@RequiredArgsConstructor
@Qualifier("noteServiceProxy")
public class PartyServiceProxy implements PartyServiceInterface, LegacyProxy {

    private final PartyService opalPartyService;
    private final LegacyPartyService legacyPartyService;
    private final DynamicConfigService dynamicConfigService;

    private PartyServiceInterface getCurrentModeService() {
        return isLegacyMode(dynamicConfigService) ? legacyPartyService : opalPartyService;
    }

    @Override
    public PartyDto getParty(long partyId) {
        return getCurrentModeService().getParty(partyId);
    }

    @Override
    public PartyDto saveParty(PartyDto partyDto) {
        return getCurrentModeService().saveParty(partyDto);
    }
}
