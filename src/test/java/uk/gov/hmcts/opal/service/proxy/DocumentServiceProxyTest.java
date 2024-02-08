package uk.gov.hmcts.opal.service.proxy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.opal.dto.AppMode;
import uk.gov.hmcts.opal.dto.search.DocumentSearchDto;
import uk.gov.hmcts.opal.entity.DocumentEntity;
import uk.gov.hmcts.opal.service.DynamicConfigService;
import uk.gov.hmcts.opal.service.legacy.LegacyDocumentService;
import uk.gov.hmcts.opal.service.opal.DocumentService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class DocumentServiceProxyTest {

    private AutoCloseable closeable;

    @Mock
    private DocumentService opalDocumentService;

    @Mock
    private LegacyDocumentService legacyDocumentService;

    @Mock
    private DynamicConfigService dynamicConfigService;

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

    @Test
    void shouldUseOpalDocumentServiceWhenModeIsNotLegacy() {
        // Given: a DocumentEntity and the app mode is set to "opal"
        DocumentEntity entity = DocumentEntity.builder().build();
        AppMode appMode = AppMode.builder().mode("opal").build();
        when(dynamicConfigService.getAppMode()).thenReturn(appMode);
        when(opalDocumentService.getDocument(anyString())).thenReturn(entity);

        // When: saveDocument is called on the proxy
        DocumentEntity documentResult = documentServiceProxy.getDocument("ID1");

        // Then: opalDocumentService should be used, and the returned document should be as expected
        verify(opalDocumentService).getDocument("ID1");
        verifyNoInteractions(legacyDocumentService);
        Assertions.assertEquals(entity, documentResult);

        // Given: a documents list result and the app mode is set to "opal"
        List<DocumentEntity> documentsList = List.of(entity);
        when(opalDocumentService.searchDocuments(any())).thenReturn(documentsList);

        // When: searchDocuments is called on the proxy
        DocumentSearchDto criteria = DocumentSearchDto.builder().build();
        List<DocumentEntity> listResult = documentServiceProxy.searchDocuments(criteria);

        // Then: opalDocumentService should be used, and the returned list should be as expected
        verify(opalDocumentService).searchDocuments(criteria);
        verifyNoInteractions(legacyDocumentService);
        Assertions.assertEquals(documentsList, listResult);
    }

    @Test
    void shouldUseLegacyDocumentServiceWhenModeIsLegacy() {
        // Given: a DocumentEntity and the app mode is set to "legacy"
        DocumentEntity entity = DocumentEntity.builder().build();
        AppMode appMode = AppMode.builder().mode("legacy").build();
        when(dynamicConfigService.getAppMode()).thenReturn(appMode);
        when(legacyDocumentService.getDocument(anyString())).thenReturn(entity);

        // When: saveDocument is called on the proxy
        DocumentEntity result = documentServiceProxy.getDocument("ID1");

        // Then: legacyDocumentService should be used, and the returned document should be as expected
        verify(legacyDocumentService).getDocument("ID1");
        verifyNoInteractions(opalDocumentService);
        Assertions.assertEquals(entity, result);

        // Given: a documents list result and the app mode is set to "legacy"
        List<DocumentEntity> documentsList = List.of(entity);
        when(legacyDocumentService.searchDocuments(any())).thenReturn(documentsList);

        // When: searchDocuments is called on the proxy
        DocumentSearchDto criteria = DocumentSearchDto.builder().build();
        List<DocumentEntity> listResult = documentServiceProxy.searchDocuments(criteria);

        // Then: opalDocumentService should be used, and the returned list should be as expected
        verify(legacyDocumentService).searchDocuments(criteria);
        verifyNoInteractions(opalDocumentService);
        Assertions.assertEquals(documentsList, listResult); // Not yet implemented in Legacy mode
    }
}
