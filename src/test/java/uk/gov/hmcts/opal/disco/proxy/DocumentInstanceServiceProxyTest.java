package uk.gov.hmcts.opal.disco.proxy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.opal.dto.search.DocumentInstanceSearchDto;
import uk.gov.hmcts.opal.entity.DocumentInstanceEntity;
import uk.gov.hmcts.opal.disco.DocumentInstanceServiceInterface;
import uk.gov.hmcts.opal.disco.legacy.LegacyDocumentInstanceService;
import uk.gov.hmcts.opal.disco.opal.DocumentInstanceService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class DocumentInstanceServiceProxyTest extends ProxyTestsBase {

    private AutoCloseable closeable;

    @Mock
    private DocumentInstanceService opalService;

    @Mock
    private LegacyDocumentInstanceService legacyService;

    @InjectMocks
    private DocumentInstanceServiceProxy documentInstanceServiceProxy;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    void testMode(DocumentInstanceServiceInterface targetService, DocumentInstanceServiceInterface otherService) {
        testGetDocumentInstance(targetService, otherService);
        testSearchDocumentInstances(targetService, otherService);
    }

    void testGetDocumentInstance(DocumentInstanceServiceInterface targetService,
                                 DocumentInstanceServiceInterface otherService) {
        // Given: a DocumentInstanceEntity is returned from the target service
        DocumentInstanceEntity entity = DocumentInstanceEntity.builder().build();
        when(targetService.getDocumentInstance(anyLong())).thenReturn(entity);

        // When: getDocumentInstance is called on the proxy
        DocumentInstanceEntity documentInstanceResult = documentInstanceServiceProxy.getDocumentInstance(1);

        // Then: target service should be used, and the returned documentInstance should be as expected
        verify(targetService).getDocumentInstance(1);
        verifyNoInteractions(otherService);
        Assertions.assertEquals(entity, documentInstanceResult);
    }

    void testSearchDocumentInstances(DocumentInstanceServiceInterface targetService,
                                     DocumentInstanceServiceInterface otherService) {
        // Given: a documentInstances list result is returned from the target service
        DocumentInstanceEntity entity = DocumentInstanceEntity.builder().build();
        List<DocumentInstanceEntity> documentInstancesList = List.of(entity);
        when(targetService.searchDocumentInstances(any())).thenReturn(documentInstancesList);

        // When: searchDocumentInstances is called on the proxy
        DocumentInstanceSearchDto criteria = DocumentInstanceSearchDto.builder().build();
        List<DocumentInstanceEntity> listResult = documentInstanceServiceProxy.searchDocumentInstances(criteria);

        // Then: target service should be used, and the returned list should be as expected
        verify(targetService).searchDocumentInstances(criteria);
        verifyNoInteractions(otherService);
        Assertions.assertEquals(documentInstancesList, listResult);
    }

    @Test
    void shouldUseOpalDocumentInstanceServiceWhenModeIsNotLegacy() {
        // Given: app mode is set
        setMode(OPAL);
        // Then: the target service is called, but the other service is not
        testMode(opalService, legacyService);
    }

    @Test
    void shouldUseLegacyDocumentInstanceServiceWhenModeIsLegacy() {
        // Given: app mode is set
        setMode(LEGACY);
        // Then: the target service is called, but the other service is not
        testMode(legacyService, opalService);
    }
}
