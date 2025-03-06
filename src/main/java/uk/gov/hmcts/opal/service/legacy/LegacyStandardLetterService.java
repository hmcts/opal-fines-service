package uk.gov.hmcts.opal.service.legacy;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;
import uk.gov.hmcts.opal.dto.search.StandardLetterSearchDto;
import uk.gov.hmcts.opal.entity.StandardLetterEntity;
import uk.gov.hmcts.opal.service.StandardLetterServiceInterface;

import java.util.List;

@Service
@Slf4j(topic = "opal.LegacyStandardLetterService")
public class LegacyStandardLetterService extends LegacyService implements StandardLetterServiceInterface {

    public LegacyStandardLetterService(LegacyGatewayProperties legacyGatewayProperties, RestClient restClient) {
        super(legacyGatewayProperties, restClient);
    }

    @Override
    public Logger getLog() {
        return log;
    }

    @Override
    public StandardLetterEntity getStandardLetter(long standardLetterId) {
        throw new LegacyGatewayResponseException("Not Yet Implemented");
    }

    @Override
    public List<StandardLetterEntity> searchStandardLetters(StandardLetterSearchDto criteria) {
        throw new LegacyGatewayResponseException("Not Yet Implemented");
    }

}
