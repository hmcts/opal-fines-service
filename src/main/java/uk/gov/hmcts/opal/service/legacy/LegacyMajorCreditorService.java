package uk.gov.hmcts.opal.service.legacy;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;
import uk.gov.hmcts.opal.dto.search.MajorCreditorSearchDto;
import uk.gov.hmcts.opal.entity.MajorCreditorEntity;
import uk.gov.hmcts.opal.service.MajorCreditorServiceInterface;

import java.util.List;

@Service
@Slf4j(topic = "LegacyMajorCreditorService")
public class LegacyMajorCreditorService extends LegacyService implements MajorCreditorServiceInterface {

    public LegacyMajorCreditorService(LegacyGatewayProperties legacyGatewayProperties, RestClient restClient) {
        super(legacyGatewayProperties, restClient);
    }

    @Override
    public Logger getLog() {
        return log;
    }

    @Override
    public MajorCreditorEntity getMajorCreditor(long majorCreditorId) {
        throw new LegacyGatewayResponseException("Not Yet Implemented");
    }

    @Override
    public List<MajorCreditorEntity> searchMajorCreditors(MajorCreditorSearchDto criteria) {
        throw new LegacyGatewayResponseException("Not Yet Implemented");
    }

}
