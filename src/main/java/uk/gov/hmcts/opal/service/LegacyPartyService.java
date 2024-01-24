package uk.gov.hmcts.opal.service;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.dto.AccountSearchDto;
import uk.gov.hmcts.opal.dto.PartyDto;
import uk.gov.hmcts.opal.entity.PartySummary;

import java.util.Collections;
import java.util.List;

@Service
@Slf4j
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
        return getFromGateway(GET_PARTY, PartyDto.class, partyId);
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


}
