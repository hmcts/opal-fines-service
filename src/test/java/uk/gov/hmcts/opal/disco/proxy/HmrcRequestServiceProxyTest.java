package uk.gov.hmcts.opal.disco.proxy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.opal.dto.search.HmrcRequestSearchDto;
import uk.gov.hmcts.opal.entity.HmrcRequestEntity;
import uk.gov.hmcts.opal.disco.HmrcRequestServiceInterface;
import uk.gov.hmcts.opal.disco.legacy.LegacyHmrcRequestService;
import uk.gov.hmcts.opal.disco.opal.HmrcRequestService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class HmrcRequestServiceProxyTest extends ProxyTestsBase {

    private AutoCloseable closeable;

    @Mock
    private HmrcRequestService opalService;

    @Mock
    private LegacyHmrcRequestService legacyService;

    @InjectMocks
    private HmrcRequestServiceProxy hmrcRequestServiceProxy;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    void testMode(HmrcRequestServiceInterface targetService, HmrcRequestServiceInterface otherService) {
        testGetHmrcRequest(targetService, otherService);
        testSearchHmrcRequests(targetService, otherService);
    }

    void testGetHmrcRequest(HmrcRequestServiceInterface targetService, HmrcRequestServiceInterface otherService) {
        // Given: a HmrcRequestEntity is returned from the target service
        HmrcRequestEntity entity = HmrcRequestEntity.builder().build();
        when(targetService.getHmrcRequest(anyLong())).thenReturn(entity);

        // When: getHmrcRequest is called on the proxy
        HmrcRequestEntity hmrcRequestResult = hmrcRequestServiceProxy.getHmrcRequest(1);

        // Then: target service should be used, and the returned hmrcRequest should be as expected
        verify(targetService).getHmrcRequest(1);
        verifyNoInteractions(otherService);
        Assertions.assertEquals(entity, hmrcRequestResult);
    }

    void testSearchHmrcRequests(HmrcRequestServiceInterface targetService, HmrcRequestServiceInterface otherService) {
        // Given: a hmrcRequests list result is returned from the target service
        HmrcRequestEntity entity = HmrcRequestEntity.builder().build();
        List<HmrcRequestEntity> hmrcRequestsList = List.of(entity);
        when(targetService.searchHmrcRequests(any())).thenReturn(hmrcRequestsList);

        // When: searchHmrcRequests is called on the proxy
        HmrcRequestSearchDto criteria = HmrcRequestSearchDto.builder().build();
        List<HmrcRequestEntity> listResult = hmrcRequestServiceProxy.searchHmrcRequests(criteria);

        // Then: target service should be used, and the returned list should be as expected
        verify(targetService).searchHmrcRequests(criteria);
        verifyNoInteractions(otherService);
        Assertions.assertEquals(hmrcRequestsList, listResult);
    }

    @Test
    void shouldUseOpalHmrcRequestServiceWhenModeIsNotLegacy() {
        // Given: app mode is set
        setMode(OPAL);
        // Then: the target service is called, but the other service is not
        testMode(opalService, legacyService);
    }

    @Test
    void shouldUseLegacyHmrcRequestServiceWhenModeIsLegacy() {
        // Given: app mode is set
        setMode(LEGACY);
        // Then: the target service is called, but the other service is not
        testMode(legacyService, opalService);
    }
}
