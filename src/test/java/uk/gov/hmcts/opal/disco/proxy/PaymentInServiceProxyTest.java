package uk.gov.hmcts.opal.disco.proxy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.opal.dto.search.PaymentInSearchDto;
import uk.gov.hmcts.opal.entity.PaymentInEntity;
import uk.gov.hmcts.opal.disco.PaymentInServiceInterface;
import uk.gov.hmcts.opal.disco.legacy.LegacyPaymentInService;
import uk.gov.hmcts.opal.disco.opal.PaymentInService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class PaymentInServiceProxyTest extends ProxyTestsBase {

    private AutoCloseable closeable;

    @Mock
    private PaymentInService opalService;

    @Mock
    private LegacyPaymentInService legacyService;

    @InjectMocks
    private PaymentInServiceProxy paymentInServiceProxy;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    void testMode(PaymentInServiceInterface targetService, PaymentInServiceInterface otherService) {
        testGetPaymentIn(targetService, otherService);
        testSearchPaymentIns(targetService, otherService);
    }

    void testGetPaymentIn(PaymentInServiceInterface targetService, PaymentInServiceInterface otherService) {
        // Given: a PaymentInEntity is returned from the target service
        PaymentInEntity entity = PaymentInEntity.builder().build();
        when(targetService.getPaymentIn(anyLong())).thenReturn(entity);

        // When: getPaymentIn is called on the proxy
        PaymentInEntity paymentInResult = paymentInServiceProxy.getPaymentIn(1);

        // Then: target service should be used, and the returned paymentIn should be as expected
        verify(targetService).getPaymentIn(1);
        verifyNoInteractions(otherService);
        Assertions.assertEquals(entity, paymentInResult);
    }

    void testSearchPaymentIns(PaymentInServiceInterface targetService, PaymentInServiceInterface otherService) {
        // Given: a paymentIns list result is returned from the target service
        PaymentInEntity entity = PaymentInEntity.builder().build();
        List<PaymentInEntity> paymentInsList = List.of(entity);
        when(targetService.searchPaymentIns(any())).thenReturn(paymentInsList);

        // When: searchPaymentIns is called on the proxy
        PaymentInSearchDto criteria = PaymentInSearchDto.builder().build();
        List<PaymentInEntity> listResult = paymentInServiceProxy.searchPaymentIns(criteria);

        // Then: target service should be used, and the returned list should be as expected
        verify(targetService).searchPaymentIns(criteria);
        verifyNoInteractions(otherService);
        Assertions.assertEquals(paymentInsList, listResult);
    }

    @Test
    void shouldUseOpalPaymentInServiceWhenModeIsNotLegacy() {
        // Given: app mode is set
        setMode(OPAL);
        // Then: the target service is called, but the other service is not
        testMode(opalService, legacyService);
    }

    @Test
    void shouldUseLegacyPaymentInServiceWhenModeIsLegacy() {
        // Given: app mode is set
        setMode(LEGACY);
        // Then: the target service is called, but the other service is not
        testMode(legacyService, opalService);
    }
}
