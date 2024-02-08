package uk.gov.hmcts.opal.service.proxy;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.DocumentSearchDto;
import uk.gov.hmcts.opal.entity.DocumentEntity;
import uk.gov.hmcts.opal.service.DocumentServiceInterface;
import uk.gov.hmcts.opal.service.DynamicConfigService;
import uk.gov.hmcts.opal.service.legacy.LegacyDocumentService;
import uk.gov.hmcts.opal.service.opal.DocumentService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Qualifier("documentServiceProxy")
public class DocumentServiceProxy implements DocumentServiceInterface, ProxyInterface {

    private final DocumentService opalDocumentService;
    private final LegacyDocumentService legacyDocumentService;
    private final DynamicConfigService dynamicConfigService;

    private DocumentServiceInterface getCurrentModeService() {
        return isLegacyMode(dynamicConfigService) ? legacyDocumentService : opalDocumentService;
    }

    @Override
    public DocumentEntity getDocument(long documentId) {
        return getCurrentModeService().getDocument(documentId);
    }

    @Override
    public List<DocumentEntity> searchDocuments(DocumentSearchDto criteria) {
        return getCurrentModeService().searchDocuments(criteria);
    }
}
