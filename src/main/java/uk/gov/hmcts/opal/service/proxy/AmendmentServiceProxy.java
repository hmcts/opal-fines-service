package uk.gov.hmcts.opal.service.proxy;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.AmendmentSearchDto;
import uk.gov.hmcts.opal.entity.AmendmentEntity;
import uk.gov.hmcts.opal.service.AmendmentServiceInterface;
import uk.gov.hmcts.opal.service.DynamicConfigService;
import uk.gov.hmcts.opal.service.legacy.LegacyAmendmentService;
import uk.gov.hmcts.opal.service.opal.AmendmentService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Qualifier("amendmentServiceProxy")
public class AmendmentServiceProxy implements AmendmentServiceInterface, ProxyInterface {

    private final AmendmentService opalAmendmentService;
    private final LegacyAmendmentService legacyAmendmentService;
    private final DynamicConfigService dynamicConfigService;

    private AmendmentServiceInterface getCurrentModeService() {
        return isLegacyMode(dynamicConfigService) ? legacyAmendmentService : opalAmendmentService;
    }

    @Override
    public AmendmentEntity getAmendment(long amendmentId) {
        return getCurrentModeService().getAmendment(amendmentId);
    }

    @Override
    public List<AmendmentEntity> searchAmendments(AmendmentSearchDto criteria) {
        return getCurrentModeService().searchAmendments(criteria);
    }
}
