package uk.gov.hmcts.opal.service.proxy;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.SuspenseItemSearchDto;
import uk.gov.hmcts.opal.entity.SuspenseItemEntity;
import uk.gov.hmcts.opal.service.DynamicConfigService;
import uk.gov.hmcts.opal.service.SuspenseItemServiceInterface;
import uk.gov.hmcts.opal.service.legacy.LegacySuspenseItemService;
import uk.gov.hmcts.opal.service.opal.SuspenseItemService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Qualifier("suspenseItemServiceProxy")
public class SuspenseItemServiceProxy implements SuspenseItemServiceInterface, ProxyInterface {

    private final SuspenseItemService opalSuspenseItemService;
    private final LegacySuspenseItemService legacySuspenseItemService;
    private final DynamicConfigService dynamicConfigService;

    private SuspenseItemServiceInterface getCurrentModeService() {
        return isLegacyMode(dynamicConfigService) ? legacySuspenseItemService : opalSuspenseItemService;
    }

    @Override
    public SuspenseItemEntity getSuspenseItem(long suspenseItemId) {
        return getCurrentModeService().getSuspenseItem(suspenseItemId);
    }

    @Override
    public List<SuspenseItemEntity> searchSuspenseItems(SuspenseItemSearchDto criteria) {
        return getCurrentModeService().searchSuspenseItems(criteria);
    }
}
