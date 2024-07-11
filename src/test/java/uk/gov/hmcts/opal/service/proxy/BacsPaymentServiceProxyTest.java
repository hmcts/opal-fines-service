package uk.gov.hmcts.opal.service.proxy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.opal.dto.search.BacsPaymentSearchDto;
import uk.gov.hmcts.opal.entity.BacsPaymentEntity;
import uk.gov.hmcts.opal.service.BacsPaymentServiceInterface;
import uk.gov.hmcts.opal.service.legacy.LegacyBacsPaymentService;
import uk.gov.hmcts.opal.service.opal.BacsPaymentService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class BacsPaymentServiceProxyTest extends ProxyTestsBase {

    private AutoCloseable closeable;

    @Mock
    private BacsPaymentService opalService;

    @Mock
    private LegacyBacsPaymentService legacyService;

    @InjectMocks
    private BacsPaymentServiceProxy bacsPaymentServiceProxy;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    void testMode(BacsPaymentServiceInterface targetService, BacsPaymentServiceInterface otherService) {
        testGetBacsPayment(targetService, otherService);
        testSearchBacsPayments(targetService, otherService);
    }

    void testGetBacsPayment(BacsPaymentServiceInterface targetService, BacsPaymentServiceInterface otherService) {
        // Given: a BacsPaymentEntity is returned from the target service
        BacsPaymentEntity entity = BacsPaymentEntity.builder().build();
        when(targetService.getBacsPayment(anyLong())).thenReturn(entity);

        // When: getBacsPayment is called on the proxy
        BacsPaymentEntity bacsPaymentResult = bacsPaymentServiceProxy.getBacsPayment(1);

        // Then: target service should be used, and the returned bacsPayment should be as expected
        verify(targetService).getBacsPayment(1);
        verifyNoInteractions(otherService);
        Assertions.assertEquals(entity, bacsPaymentResult);
    }

    void testSearchBacsPayments(BacsPaymentServiceInterface targetService, BacsPaymentServiceInterface otherService) {
        // Given: a bacsPayments list result is returned from the target service
        BacsPaymentEntity entity = BacsPaymentEntity.builder().build();
        List<BacsPaymentEntity> bacsPaymentsList = List.of(entity);
        when(targetService.searchBacsPayments(any())).thenReturn(bacsPaymentsList);

        // When: searchBacsPayments is called on the proxy
        BacsPaymentSearchDto criteria = BacsPaymentSearchDto.builder().build();
        List<BacsPaymentEntity> listResult = bacsPaymentServiceProxy.searchBacsPayments(criteria);

        // Then: target service should be used, and the returned list should be as expected
        verify(targetService).searchBacsPayments(criteria);
        verifyNoInteractions(otherService);
        Assertions.assertEquals(bacsPaymentsList, listResult);
    }

    @Test
    void shouldUseOpalBacsPaymentServiceWhenModeIsNotLegacy() {
        // Given: app mode is set
        setMode(OPAL);
        // Then: the target service is called, but the other service is not
        testMode(opalService, legacyService);
    }

    @Test
    void shouldUseLegacyBacsPaymentServiceWhenModeIsLegacy() {
        // Given: app mode is set
        setMode(LEGACY);
        // Then: the target service is called, but the other service is not
        testMode(legacyService, opalService);
    }
}
