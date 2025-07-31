package uk.gov.hmcts.opal.disco.proxy;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.DocumentSearchDto;
import uk.gov.hmcts.opal.entity.DocumentEntity;
import uk.gov.hmcts.opal.disco.DocumentServiceInterface;
import uk.gov.hmcts.opal.service.opal.DynamicConfigService;
import uk.gov.hmcts.opal.disco.legacy.LegacyDocumentService;
import uk.gov.hmcts.opal.disco.opal.DocumentService;

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
    public DocumentEntity getDocument(String documentId) {
        return getCurrentModeService().getDocument(documentId);
    }

    @Override
    public List<DocumentEntity> searchDocuments(DocumentSearchDto criteria) {
        return getCurrentModeService().searchDocuments(criteria);
    }
}
