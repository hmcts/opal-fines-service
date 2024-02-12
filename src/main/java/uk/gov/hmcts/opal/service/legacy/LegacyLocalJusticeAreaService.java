package uk.gov.hmcts.opal.service.legacy;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.dto.search.LocalJusticeAreaSearchDto;
import uk.gov.hmcts.opal.entity.LocalJusticeAreaEntity;
import uk.gov.hmcts.opal.service.LocalJusticeAreaServiceInterface;

import java.util.List;

@Service
@Slf4j(topic = "LegacyLocalJusticeAreaService")
public class LegacyLocalJusticeAreaService extends LegacyService implements LocalJusticeAreaServiceInterface {

    @Autowired
    protected LegacyLocalJusticeAreaService(@Value("${legacy-gateway.url}") String gatewayUrl, RestClient restClient) {
        super(gatewayUrl, restClient);
    }

    @Override
    public Logger getLog() {
        return log;
    }

    @Override
    public LocalJusticeAreaEntity getLocalJusticeArea(long localJusticeAreaId) {
        throw new LegacyGatewayResponseException("Not Yet Implemented");
    }

    @Override
    public List<LocalJusticeAreaEntity> searchLocalJusticeAreas(LocalJusticeAreaSearchDto criteria) {
        throw new LegacyGatewayResponseException("Not Yet Implemented");
    }

}
