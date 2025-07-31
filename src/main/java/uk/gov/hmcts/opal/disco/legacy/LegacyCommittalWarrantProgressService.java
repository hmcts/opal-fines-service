package uk.gov.hmcts.opal.disco.legacy;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;
import uk.gov.hmcts.opal.dto.legacy.search.LegacyCommittalWarrantProgressSearchResults;
import uk.gov.hmcts.opal.dto.search.CommittalWarrantProgressSearchDto;
import uk.gov.hmcts.opal.entity.CommittalWarrantProgressEntity;
import uk.gov.hmcts.opal.disco.CommittalWarrantProgressServiceInterface;

import java.util.List;

@Service
@Slf4j(topic = "opal.LegacyCommittalWarrantProgressService")
public class LegacyCommittalWarrantProgressService extends LegacyService
    implements CommittalWarrantProgressServiceInterface {


    public LegacyCommittalWarrantProgressService(LegacyGatewayProperties legacyGatewayProperties,
                                                 RestClient restClient) {
        super(legacyGatewayProperties, restClient);
    }

    @Override
    public Logger getLog() {
        return log;
    }

    @Override
    public CommittalWarrantProgressEntity getCommittalWarrantProgress(long committalWarrantProgressId) {
        log.debug("getCommittalWarrantProgress for {} from {}", committalWarrantProgressId, legacyGateway.getUrl());
        return postToGateway("getCommittalWarrantProgress", CommittalWarrantProgressEntity.class,
            committalWarrantProgressId);
    }

    @Override
    public List<CommittalWarrantProgressEntity> searchCommittalWarrantProgresss(
        CommittalWarrantProgressSearchDto criteria) {
        log.debug(":searchCommittalWarrantProgresss: criteria: {} via gateway {}", criteria.toJson(),
            legacyGateway.getUrl());
        return postToGateway("searchCommittalWarrantProgress",
                             LegacyCommittalWarrantProgressSearchResults.class, criteria)
            .getCommittalWarrantProgressEntities();
    }

}
