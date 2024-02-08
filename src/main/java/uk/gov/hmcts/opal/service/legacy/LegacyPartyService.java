package uk.gov.hmcts.opal.service.legacy;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.dto.search.AccountSearchDto;
import uk.gov.hmcts.opal.dto.PartyDto;
import uk.gov.hmcts.opal.dto.search.PartySearchDto;
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.entity.PartySummary;
import uk.gov.hmcts.opal.service.PartyServiceInterface;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@Slf4j(topic = "LegacyPartyService")
public class LegacyPartyService extends LegacyService implements PartyServiceInterface {

    public static final String GET_PARTY = "getParty";
    public static final String POST_PARTY = "postParty";

    @Autowired
    protected LegacyPartyService(@Value("${legacy-gateway-url}") String gatewayUrl, RestClient restClient) {
        super(gatewayUrl, restClient);
    }

    @Override
    public Logger getLog() {
        return log;
    }

    @Override
    public PartyDto getParty(long partyId) {
        log.info("Get party for id: {}", partyId);
        return postParamsToGateway(GET_PARTY, PartyDto.class, Map.of("party_id", partyId));
    }

    @Override
    public PartyDto saveParty(PartyDto partyDto) {
        log.info("Sending party to {}", gatewayUrl);
        return postToGateway(POST_PARTY, PartyDto.class, partyDto);
    }

    @Override
    public List<PartySummary> searchForParty(AccountSearchDto accountSearchDto) {
        return Collections.<PartySummary>emptyList();
    }

    @Override
    public List<PartyEntity> searchParties(PartySearchDto criteria) {
        throw new LegacyGatewayResponseException("Not Yet Implemented");
    }


}
