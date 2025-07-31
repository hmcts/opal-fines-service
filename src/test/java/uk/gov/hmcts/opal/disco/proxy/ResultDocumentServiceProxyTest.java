package uk.gov.hmcts.opal.disco.proxy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.opal.dto.search.ResultDocumentSearchDto;
import uk.gov.hmcts.opal.entity.ResultDocumentEntity;
import uk.gov.hmcts.opal.disco.ResultDocumentServiceInterface;
import uk.gov.hmcts.opal.disco.legacy.LegacyResultDocumentService;
import uk.gov.hmcts.opal.disco.opal.ResultDocumentService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ResultDocumentServiceProxyTest extends ProxyTestsBase {

    private AutoCloseable closeable;

    @Mock
    private ResultDocumentService opalService;

    @Mock
    private LegacyResultDocumentService legacyService;

    @InjectMocks
    private ResultDocumentServiceProxy resultDocumentServiceProxy;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    void testMode(ResultDocumentServiceInterface targetService, ResultDocumentServiceInterface otherService) {
        testGetResultDocument(targetService, otherService);
        testSearchResultDocuments(targetService, otherService);
    }

    void testGetResultDocument(ResultDocumentServiceInterface targetService,
                               ResultDocumentServiceInterface otherService) {
        // Given: a ResultDocumentEntity is returned from the target service
        ResultDocumentEntity entity = ResultDocumentEntity.builder().build();
        when(targetService.getResultDocument(anyLong())).thenReturn(entity);

        // When: getResultDocument is called on the proxy
        ResultDocumentEntity resultDocumentResult = resultDocumentServiceProxy.getResultDocument(1);

        // Then: target service should be used, and the returned resultDocument should be as expected
        verify(targetService).getResultDocument(1);
        verifyNoInteractions(otherService);
        Assertions.assertEquals(entity, resultDocumentResult);
    }

    void testSearchResultDocuments(ResultDocumentServiceInterface targetService,
                                   ResultDocumentServiceInterface otherService) {
        // Given: a resultDocuments list result is returned from the target service
        ResultDocumentEntity entity = ResultDocumentEntity.builder().build();
        List<ResultDocumentEntity> resultDocumentsList = List.of(entity);
        when(targetService.searchResultDocuments(any())).thenReturn(resultDocumentsList);

        // When: searchResultDocuments is called on the proxy
        ResultDocumentSearchDto criteria = ResultDocumentSearchDto.builder().build();
        List<ResultDocumentEntity> listResult = resultDocumentServiceProxy.searchResultDocuments(criteria);

        // Then: target service should be used, and the returned list should be as expected
        verify(targetService).searchResultDocuments(criteria);
        verifyNoInteractions(otherService);
        Assertions.assertEquals(resultDocumentsList, listResult);
    }

    @Test
    void shouldUseOpalResultDocumentServiceWhenModeIsNotLegacy() {
        // Given: app mode is set
        setMode(OPAL);
        // Then: the target service is called, but the other service is not
        testMode(opalService, legacyService);
    }

    @Test
    void shouldUseLegacyResultDocumentServiceWhenModeIsLegacy() {
        // Given: app mode is set
        setMode(LEGACY);
        // Then: the target service is called, but the other service is not
        testMode(legacyService, opalService);
    }
}
