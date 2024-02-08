package uk.gov.hmcts.opal.service.legacy;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.dto.search.CommittalWarrantProgressSearchDto;
import uk.gov.hmcts.opal.entity.CommittalWarrantProgressEntity;
import uk.gov.hmcts.opal.service.CommittalWarrantProgressServiceInterface;

import java.util.List;

@Service
@Slf4j(topic = "LegacyCommittalWarrantProgressService")
public class LegacyCommittalWarrantProgressService extends LegacyService
    implements CommittalWarrantProgressServiceInterface {

    @Autowired
    protected LegacyCommittalWarrantProgressService(@Value("${legacy-gateway-url}") String gatewayUrl,
                                                    RestClient restClient) {
        super(gatewayUrl, restClient);
    }

    @Override
    public Logger getLog() {
        return log;
    }

    @Override
    public CommittalWarrantProgressEntity getCommittalWarrantProgress(long committalWarrantProgressId) {
        throw new LegacyGatewayResponseException("Not Yet Implemented");
    }

    @Override
    public List<CommittalWarrantProgressEntity> searchCommittalWarrantProgresss(
        CommittalWarrantProgressSearchDto criteria) {
        throw new LegacyGatewayResponseException("Not Yet Implemented");
    }

}
