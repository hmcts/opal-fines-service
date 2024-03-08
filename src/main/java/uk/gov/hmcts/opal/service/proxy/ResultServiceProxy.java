package uk.gov.hmcts.opal.service.proxy;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.ResultSearchDto;
import uk.gov.hmcts.opal.entity.ResultEntity;
import uk.gov.hmcts.opal.service.DynamicConfigService;
import uk.gov.hmcts.opal.service.ResultServiceInterface;
import uk.gov.hmcts.opal.service.legacy.LegacyResultService;
import uk.gov.hmcts.opal.service.opal.ResultService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Qualifier("resultServiceProxy")
public class ResultServiceProxy implements ResultServiceInterface, ProxyInterface {

    private final ResultService opalResultService;
    private final LegacyResultService legacyResultService;
    private final DynamicConfigService dynamicConfigService;

    private ResultServiceInterface getCurrentModeService() {
        return isLegacyMode(dynamicConfigService) ? legacyResultService : opalResultService;
    }

    @Override
    public ResultEntity getResult(long resultId) {
        return getCurrentModeService().getResult(resultId);
    }

    @Override
    public List<ResultEntity> searchResults(ResultSearchDto criteria) {
        return getCurrentModeService().searchResults(criteria);
    }
}
