package uk.gov.hmcts.opal.disco.proxy;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.DocumentInstanceSearchDto;
import uk.gov.hmcts.opal.entity.DocumentInstanceEntity;
import uk.gov.hmcts.opal.disco.DocumentInstanceServiceInterface;
import uk.gov.hmcts.opal.service.opal.DynamicConfigService;
import uk.gov.hmcts.opal.disco.legacy.LegacyDocumentInstanceService;
import uk.gov.hmcts.opal.disco.opal.DocumentInstanceService;

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
