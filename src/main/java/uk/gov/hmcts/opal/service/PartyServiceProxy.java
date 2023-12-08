package uk.gov.hmcts.opal.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.AccountSearchDto;
import uk.gov.hmcts.opal.dto.PartyDto;
import uk.gov.hmcts.opal.entity.PartySummary;

import java.util.List;

@Service
@RequiredArgsConstructor
@Qualifier("partyServiceProxy")
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

    @Override
    public List<PartySummary> searchForParty(AccountSearchDto accountSearchDto) {
        return getCurrentModeService().searchForParty(accountSearchDto);
    }
}
