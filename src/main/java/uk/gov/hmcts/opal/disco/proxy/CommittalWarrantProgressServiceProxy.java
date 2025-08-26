package uk.gov.hmcts.opal.disco.proxy;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.CommittalWarrantProgressSearchDto;
import uk.gov.hmcts.opal.entity.CommittalWarrantProgressEntity;
import uk.gov.hmcts.opal.disco.CommittalWarrantProgressServiceInterface;
import uk.gov.hmcts.opal.service.opal.DynamicConfigService;
import uk.gov.hmcts.opal.disco.legacy.LegacyCommittalWarrantProgressService;
import uk.gov.hmcts.opal.disco.opal.CommittalWarrantProgressService;
import uk.gov.hmcts.opal.service.proxy.ProxyInterface;

import java.util.List;

@Service
@RequiredArgsConstructor
@Qualifier("committalWarrantProgressServiceProxy")
public class CommittalWarrantProgressServiceProxy implements CommittalWarrantProgressServiceInterface, ProxyInterface {

    private final CommittalWarrantProgressService opalCommittalWarrantProgressService;
    private final LegacyCommittalWarrantProgressService legacyCommittalWarrantProgressService;
    private final DynamicConfigService dynamicConfigService;

    private CommittalWarrantProgressServiceInterface getCurrentModeService() {
        return isLegacyMode(dynamicConfigService) ? legacyCommittalWarrantProgressService :
            opalCommittalWarrantProgressService;
    }

    @Override
    public CommittalWarrantProgressEntity getCommittalWarrantProgress(long committalWarrantProgressId) {
        return getCurrentModeService().getCommittalWarrantProgress(committalWarrantProgressId);
    }

    @Override
    public List<CommittalWarrantProgressEntity> searchCommittalWarrantProgresss(
        CommittalWarrantProgressSearchDto criteria) {
        return getCurrentModeService().searchCommittalWarrantProgresss(criteria);
    }
}
