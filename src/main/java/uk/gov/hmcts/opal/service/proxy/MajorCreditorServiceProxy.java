package uk.gov.hmcts.opal.service.proxy;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.MajorCreditorSearchDto;
import uk.gov.hmcts.opal.entity.MajorCreditorEntity;
import uk.gov.hmcts.opal.service.DynamicConfigService;
import uk.gov.hmcts.opal.service.MajorCreditorServiceInterface;
import uk.gov.hmcts.opal.service.legacy.LegacyMajorCreditorService;
import uk.gov.hmcts.opal.service.opal.MajorCreditorService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Qualifier("majorCreditorServiceProxy")
public class MajorCreditorServiceProxy implements MajorCreditorServiceInterface, ProxyInterface {

    private final MajorCreditorService opalMajorCreditorService;
    private final LegacyMajorCreditorService legacyMajorCreditorService;
    private final DynamicConfigService dynamicConfigService;

    private MajorCreditorServiceInterface getCurrentModeService() {
        return isLegacyMode(dynamicConfigService) ? legacyMajorCreditorService : opalMajorCreditorService;
    }

    @Override
    public MajorCreditorEntity getMajorCreditor(long majorCreditorId) {
        return getCurrentModeService().getMajorCreditor(majorCreditorId);
    }

    @Override
    public List<MajorCreditorEntity> searchMajorCreditors(MajorCreditorSearchDto criteria) {
        return getCurrentModeService().searchMajorCreditors(criteria);
    }
}
