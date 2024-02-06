package uk.gov.hmcts.opal.service.proxy;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.DocumentInstanceSearchDto;
import uk.gov.hmcts.opal.entity.DocumentInstanceEntity;
import uk.gov.hmcts.opal.service.DocumentInstanceServiceInterface;
import uk.gov.hmcts.opal.service.DynamicConfigService;
import uk.gov.hmcts.opal.service.legacy.LegacyDocumentInstanceService;
import uk.gov.hmcts.opal.service.opal.DocumentInstanceService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Qualifier("documentInstanceServiceProxy")
public class DocumentInstanceServiceProxy implements DocumentInstanceServiceInterface, ProxyInterface {

    private final DocumentInstanceService opalDocumentInstanceService;
    private final LegacyDocumentInstanceService legacyDocumentInstanceService;
    private final DynamicConfigService dynamicConfigService;

    private DocumentInstanceServiceInterface getCurrentModeService() {
        return isLegacyMode(dynamicConfigService) ? legacyDocumentInstanceService : opalDocumentInstanceService;
    }

    @Override
    public DocumentInstanceEntity getDocumentInstance(long documentInstanceId) {
        return getCurrentModeService().getDocumentInstance(documentInstanceId);
    }

    @Override
    public List<DocumentInstanceEntity> searchDocumentInstances(DocumentInstanceSearchDto criteria) {
        return getCurrentModeService().searchDocumentInstances(criteria);
    }
}
