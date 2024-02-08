package uk.gov.hmcts.opal.service.proxy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.opal.dto.AppMode;
import uk.gov.hmcts.opal.dto.search.DocumentInstanceSearchDto;
import uk.gov.hmcts.opal.entity.DocumentInstanceEntity;
import uk.gov.hmcts.opal.service.DynamicConfigService;
import uk.gov.hmcts.opal.service.legacy.LegacyDocumentInstanceService;
import uk.gov.hmcts.opal.service.opal.DocumentInstanceService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class DocumentInstanceServiceProxyTest {

    private AutoCloseable closeable;

    @Mock
    private DocumentInstanceService opalDocumentInstanceService;

    @Mock
    private LegacyDocumentInstanceService legacyDocumentInstanceService;

    @Mock
    private DynamicConfigService dynamicConfigService;

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

    @Test
    void shouldUseOpalDocumentInstanceServiceWhenModeIsNotLegacy() {
        // Given: a DocumentInstanceEntity and the app mode is set to "opal"
        DocumentInstanceEntity entity = DocumentInstanceEntity.builder().build();
        AppMode appMode = AppMode.builder().mode("opal").build();
        when(dynamicConfigService.getAppMode()).thenReturn(appMode);
        when(opalDocumentInstanceService.getDocumentInstance(anyLong())).thenReturn(entity);

        // When: saveDocumentInstance is called on the proxy
        DocumentInstanceEntity documentInstanceResult = documentInstanceServiceProxy.getDocumentInstance(1);

        // Then: opalDocumentInstanceService should be used, and the returned documentInstance should be as expected
        verify(opalDocumentInstanceService).getDocumentInstance(1);
        verifyNoInteractions(legacyDocumentInstanceService);
        Assertions.assertEquals(entity, documentInstanceResult);

        // Given: a documentInstances list result and the app mode is set to "opal"
        List<DocumentInstanceEntity> documentInstancesList = List.of(entity);
        when(opalDocumentInstanceService.searchDocumentInstances(any())).thenReturn(documentInstancesList);

        // When: searchDocumentInstances is called on the proxy
        DocumentInstanceSearchDto criteria = DocumentInstanceSearchDto.builder().build();
        List<DocumentInstanceEntity> listResult = documentInstanceServiceProxy.searchDocumentInstances(criteria);

        // Then: opalDocumentInstanceService should be used, and the returned list should be as expected
        verify(opalDocumentInstanceService).searchDocumentInstances(criteria);
        verifyNoInteractions(legacyDocumentInstanceService);
        Assertions.assertEquals(documentInstancesList, listResult);
    }

    @Test
    void shouldUseLegacyDocumentInstanceServiceWhenModeIsLegacy() {
        // Given: a DocumentInstanceEntity and the app mode is set to "legacy"
        DocumentInstanceEntity entity = DocumentInstanceEntity.builder().build();
        AppMode appMode = AppMode.builder().mode("legacy").build();
        when(dynamicConfigService.getAppMode()).thenReturn(appMode);
        when(legacyDocumentInstanceService.getDocumentInstance(anyLong())).thenReturn(entity);

        // When: saveDocumentInstance is called on the proxy
        DocumentInstanceEntity result = documentInstanceServiceProxy.getDocumentInstance(1);

        // Then: legacyDocumentInstanceService should be used, and the returned documentInstance should be as expected
        verify(legacyDocumentInstanceService).getDocumentInstance(1);
        verifyNoInteractions(opalDocumentInstanceService);
        Assertions.assertEquals(entity, result);

        // Given: a documentInstances list result and the app mode is set to "legacy"
        List<DocumentInstanceEntity> documentInstancesList = List.of(entity);
        when(legacyDocumentInstanceService.searchDocumentInstances(any())).thenReturn(documentInstancesList);

        // When: searchDocumentInstances is called on the proxy
        DocumentInstanceSearchDto criteria = DocumentInstanceSearchDto.builder().build();
        List<DocumentInstanceEntity> listResult = documentInstanceServiceProxy.searchDocumentInstances(criteria);

        // Then: opalDocumentInstanceService should be used, and the returned list should be as expected
        verify(legacyDocumentInstanceService).searchDocumentInstances(criteria);
        verifyNoInteractions(opalDocumentInstanceService);
        Assertions.assertEquals(documentInstancesList, listResult); // Not yet implemented in Legacy mode
    }
}
