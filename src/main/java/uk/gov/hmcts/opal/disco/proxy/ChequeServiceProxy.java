package uk.gov.hmcts.opal.disco.proxy;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.ChequeSearchDto;
import uk.gov.hmcts.opal.entity.ChequeEntity;
import uk.gov.hmcts.opal.service.opal.DynamicConfigService;
import uk.gov.hmcts.opal.disco.ChequeServiceInterface;
import uk.gov.hmcts.opal.disco.legacy.LegacyChequeService;
import uk.gov.hmcts.opal.disco.opal.ChequeService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Qualifier("chequeServiceProxy")
public class ChequeServiceProxy implements ChequeServiceInterface, ProxyInterface {

    private final ChequeService opalChequeService;
    private final LegacyChequeService legacyChequeService;
    private final DynamicConfigService dynamicConfigService;

    private ChequeServiceInterface getCurrentModeService() {
        return isLegacyMode(dynamicConfigService) ? legacyChequeService : opalChequeService;
    }

    @Override
    public ChequeEntity getCheque(long chequeId) {
        return getCurrentModeService().getCheque(chequeId);
    }

    @Override
    public List<ChequeEntity> searchCheques(ChequeSearchDto criteria) {
        return getCurrentModeService().searchCheques(criteria);
    }
}
