package uk.gov.hmcts.opal.service.proxy;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.CommittalWarrantProgressSearchDto;
import uk.gov.hmcts.opal.entity.CommittalWarrantProgressEntity;
import uk.gov.hmcts.opal.service.CommittalWarrantProgressServiceInterface;
import uk.gov.hmcts.opal.service.DynamicConfigService;
import uk.gov.hmcts.opal.service.legacy.LegacyCommittalWarrantProgressService;
import uk.gov.hmcts.opal.service.opal.CommittalWarrantProgressService;

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
