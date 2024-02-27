package uk.gov.hmcts.opal.service.proxy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.opal.dto.search.ResultSearchDto;
import uk.gov.hmcts.opal.entity.ResultEntity;
import uk.gov.hmcts.opal.service.ResultServiceInterface;
import uk.gov.hmcts.opal.service.legacy.LegacyResultService;
import uk.gov.hmcts.opal.service.opal.ResultService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ResultServiceProxyTest extends ProxyTestsBase {

    private AutoCloseable closeable;

    @Mock
    private ResultService opalService;

    @Mock
    private LegacyResultService legacyService;

    @InjectMocks
    private ResultServiceProxy resultServiceProxy;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    void testMode(ResultServiceInterface targetService, ResultServiceInterface otherService) {
        testGetResult(targetService, otherService);
        testSearchResults(targetService, otherService);
    }

    void testGetResult(ResultServiceInterface targetService, ResultServiceInterface otherService) {
        // Given: a ResultEntity is returned from the target service
        ResultEntity entity = ResultEntity.builder().build();
        when(targetService.getResult(anyLong())).thenReturn(entity);

        // When: getResult is called on the proxy
        ResultEntity resultResult = resultServiceProxy.getResult(1);

        // Then: target service should be used, and the returned result should be as expected
        verify(targetService).getResult(1);
        verifyNoInteractions(otherService);
        Assertions.assertEquals(entity, resultResult);
    }

    void testSearchResults(ResultServiceInterface targetService, ResultServiceInterface otherService) {
        // Given: a results list result is returned from the target service
        ResultEntity entity = ResultEntity.builder().build();
        List<ResultEntity> resultsList = List.of(entity);
        when(targetService.searchResults(any())).thenReturn(resultsList);

        // When: searchResults is called on the proxy
        ResultSearchDto criteria = ResultSearchDto.builder().build();
        List<ResultEntity> listResult = resultServiceProxy.searchResults(criteria);

        // Then: target service should be used, and the returned list should be as expected
        verify(targetService).searchResults(criteria);
        verifyNoInteractions(otherService);
        Assertions.assertEquals(resultsList, listResult);
    }

    @Test
    void shouldUseOpalResultServiceWhenModeIsNotLegacy() {
        // Given: app mode is set
        setMode(OPAL);
        // Then: the target service is called, but the other service is not
        testMode(opalService, legacyService);
    }

    @Test
    void shouldUseLegacyResultServiceWhenModeIsLegacy() {
        // Given: app mode is set
        setMode(LEGACY);
        // Then: the target service is called, but the other service is not
        testMode(legacyService, opalService);
    }
}
