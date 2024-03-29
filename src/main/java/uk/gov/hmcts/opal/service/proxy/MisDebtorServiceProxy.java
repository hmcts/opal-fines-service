package uk.gov.hmcts.opal.service.proxy;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.MisDebtorSearchDto;
import uk.gov.hmcts.opal.entity.MisDebtorEntity;
import uk.gov.hmcts.opal.service.DynamicConfigService;
import uk.gov.hmcts.opal.service.MisDebtorServiceInterface;
import uk.gov.hmcts.opal.service.legacy.LegacyMisDebtorService;
import uk.gov.hmcts.opal.service.opal.MisDebtorService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Qualifier("misDebtorServiceProxy")
public class MisDebtorServiceProxy implements MisDebtorServiceInterface, ProxyInterface {

    private final MisDebtorService opalMisDebtorService;
    private final LegacyMisDebtorService legacyMisDebtorService;
    private final DynamicConfigService dynamicConfigService;

    private MisDebtorServiceInterface getCurrentModeService() {
        return isLegacyMode(dynamicConfigService) ? legacyMisDebtorService : opalMisDebtorService;
    }

    @Override
    public MisDebtorEntity getMisDebtor(long misDebtorId) {
        return getCurrentModeService().getMisDebtor(misDebtorId);
    }

    @Override
    public List<MisDebtorEntity> searchMisDebtors(MisDebtorSearchDto criteria) {
        return getCurrentModeService().searchMisDebtors(criteria);
    }
}
