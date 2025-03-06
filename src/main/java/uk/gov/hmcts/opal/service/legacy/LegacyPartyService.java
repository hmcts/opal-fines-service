package uk.gov.hmcts.opal.service.legacy;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;
import uk.gov.hmcts.opal.dto.PartyDto;
import uk.gov.hmcts.opal.dto.legacy.search.LegacyPartySearchResults;
import uk.gov.hmcts.opal.dto.search.AccountSearchDto;
import uk.gov.hmcts.opal.dto.search.PartySearchDto;
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.entity.PartySummary;
import uk.gov.hmcts.opal.service.PartyServiceInterface;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@Slf4j(topic = "opal.LegacyPartyService")
public class LegacyPartyService extends LegacyService implements PartyServiceInterface {

    public static final String GET_PARTY = "getParty";
    public static final String POST_PARTY = "postParty";

    public LegacyPartyService(LegacyGatewayProperties legacyGateway, RestClient restClient) {
        super(legacyGateway, restClient);
    }

    @Override
    public Logger getLog() {
        return log;
    }

    @Override
    public PartyDto getParty(long partyId) {
        log.debug("Get party for id: {}", partyId);
        return postParamsToGateway(GET_PARTY, PartyDto.class, Map.of("party_id", partyId));
    }

    @Override
    public PartyDto saveParty(PartyDto partyDto) {
        log.debug("Sending party to {}", legacyGateway.getUrl());
        return postToGateway(POST_PARTY, PartyDto.class, partyDto);
    }

    @Override
    public List<PartySummary> searchForParty(AccountSearchDto accountSearchDto) {
        return Collections.emptyList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<PartyEntity> searchParties(PartySearchDto criteria) {
        log.debug(":searchParties: criteria: {} via gateway {}", criteria.toJson(), legacyGateway.getUrl());
        return postToGateway("searchParties", LegacyPartySearchResults.class, criteria)
            .getPartyEntities();
    }


}
