package uk.gov.hmcts.opal.service.proxy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.opal.dto.search.DocumentSearchDto;
import uk.gov.hmcts.opal.entity.DocumentEntity;
import uk.gov.hmcts.opal.service.DocumentServiceInterface;
import uk.gov.hmcts.opal.service.legacy.LegacyDocumentService;
import uk.gov.hmcts.opal.service.opal.DocumentService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class DocumentServiceProxyTest extends ProxyTestsBase {

    private AutoCloseable closeable;

    @Mock
    private DocumentService opalService;

    @Mock
    private LegacyDocumentService legacyService;

    @InjectMocks
    private DocumentServiceProxy documentServiceProxy;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    void testMode(DocumentServiceInterface targetService, DocumentServiceInterface otherService) {
        testGetDocument(targetService, otherService);
        testSearchDocuments(targetService, otherService);
    }

    void testGetDocument(DocumentServiceInterface targetService, DocumentServiceInterface otherService) {
        // Given: a DocumentEntity is returned from the target service
        DocumentEntity entity = DocumentEntity.builder().build();
        when(targetService.getDocument(anyString())).thenReturn(entity);

        // When: getDocument is called on the proxy
        DocumentEntity documentResult = documentServiceProxy.getDocument("1");

        // Then: target service should be used, and the returned document should be as expected
        verify(targetService).getDocument("1");
        verifyNoInteractions(otherService);
        Assertions.assertEquals(entity, documentResult);
    }

    void testSearchDocuments(DocumentServiceInterface targetService, DocumentServiceInterface otherService) {
        // Given: a documents list result is returned from the target service
        DocumentEntity entity = DocumentEntity.builder().build();
        List<DocumentEntity> documentsList = List.of(entity);
        when(targetService.searchDocuments(any())).thenReturn(documentsList);

        // When: searchDocuments is called on the proxy
        DocumentSearchDto criteria = DocumentSearchDto.builder().build();
        List<DocumentEntity> listResult = documentServiceProxy.searchDocuments(criteria);

        // Then: target service should be used, and the returned list should be as expected
        verify(targetService).searchDocuments(criteria);
        verifyNoInteractions(otherService);
        Assertions.assertEquals(documentsList, listResult);
    }

    @Test
    void shouldUseOpalDocumentServiceWhenModeIsNotLegacy() {
        // Given: app mode is set
        setMode(OPAL);
        // Then: the target service is called, but the other service is not
        testMode(opalService, legacyService);
    }

    @Test
    void shouldUseLegacyDocumentServiceWhenModeIsLegacy() {
        // Given: app mode is set
        setMode(LEGACY);
        // Then: the target service is called, but the other service is not
        testMode(legacyService, opalService);
    }
}
