package uk.gov.hmcts.opal.service.legacy;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;
import uk.gov.hmcts.opal.dto.legacy.search.LegacyMajorCreditorSearchResults;
import uk.gov.hmcts.opal.dto.search.MajorCreditorSearchDto;
import uk.gov.hmcts.opal.entity.MajorCreditorEntity;
import uk.gov.hmcts.opal.service.MajorCreditorServiceInterface;

import java.util.List;

@Service
@Slf4j(topic = "opal.LegacyMajorCreditorService")
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
        log.debug("getMajorCreditor for {} from {}", majorCreditorId, legacyGateway.getUrl());
        return postToGateway("getMajorCreditor", MajorCreditorEntity.class, majorCreditorId);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<MajorCreditorEntity> searchMajorCreditors(MajorCreditorSearchDto criteria) {
        log.debug(":searchMajorCreditors: criteria: {} via gateway {}", criteria.toJson(), legacyGateway.getUrl());
        return postToGateway("searchMajorCreditors", LegacyMajorCreditorSearchResults.class, criteria)
            .getMajorCreditorEntities();
    }

}
