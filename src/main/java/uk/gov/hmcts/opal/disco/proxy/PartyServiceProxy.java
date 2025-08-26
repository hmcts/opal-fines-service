package uk.gov.hmcts.opal.disco.proxy;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.PartyDto;
import uk.gov.hmcts.opal.dto.search.AccountSearchDto;
import uk.gov.hmcts.opal.dto.search.PartySearchDto;
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.entity.PartySummary;
import uk.gov.hmcts.opal.service.opal.DynamicConfigService;
import uk.gov.hmcts.opal.disco.PartyServiceInterface;
import uk.gov.hmcts.opal.disco.legacy.LegacyPartyService;
import uk.gov.hmcts.opal.disco.opal.PartyService;
import uk.gov.hmcts.opal.service.proxy.ProxyInterface;

import java.util.List;

@Service
@RequiredArgsConstructor
@Qualifier("partyServiceProxy")
public class PartyServiceProxy implements PartyServiceInterface, ProxyInterface {

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

    @Override
    public List<PartyEntity> searchParties(PartySearchDto criteria) {
        return getCurrentModeService().searchParties(criteria);
    }
}
