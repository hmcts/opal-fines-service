package uk.gov.hmcts.opal.service.proxy;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.ResultDocumentSearchDto;
import uk.gov.hmcts.opal.entity.ResultDocumentEntity;
import uk.gov.hmcts.opal.service.DynamicConfigService;
import uk.gov.hmcts.opal.service.ResultDocumentServiceInterface;
import uk.gov.hmcts.opal.service.legacy.LegacyResultDocumentService;
import uk.gov.hmcts.opal.service.opal.ResultDocumentService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Qualifier("resultDocumentServiceProxy")
public class ResultDocumentServiceProxy implements ResultDocumentServiceInterface, ProxyInterface {

    private final ResultDocumentService opalResultDocumentService;
    private final LegacyResultDocumentService legacyResultDocumentService;
    private final DynamicConfigService dynamicConfigService;

    private ResultDocumentServiceInterface getCurrentModeService() {
        return isLegacyMode(dynamicConfigService) ? legacyResultDocumentService : opalResultDocumentService;
    }

    @Override
    public ResultDocumentEntity getResultDocument(long resultDocumentId) {
        return getCurrentModeService().getResultDocument(resultDocumentId);
    }

    @Override
    public List<ResultDocumentEntity> searchResultDocuments(ResultDocumentSearchDto criteria) {
        return getCurrentModeService().searchResultDocuments(criteria);
    }
}
