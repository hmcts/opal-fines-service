package uk.gov.hmcts.opal.disco.proxy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.opal.dto.search.PaymentTermsSearchDto;
import uk.gov.hmcts.opal.entity.PaymentTermsEntity;
import uk.gov.hmcts.opal.disco.PaymentTermsServiceInterface;
import uk.gov.hmcts.opal.disco.legacy.LegacyPaymentTermsService;
import uk.gov.hmcts.opal.disco.opal.PaymentTermsService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class PaymentTermsServiceProxyTest extends ProxyTestsBase {

    private AutoCloseable closeable;

    @Mock
    private PaymentTermsService opalService;

    @Mock
    private LegacyPaymentTermsService legacyService;

    @InjectMocks
    private PaymentTermsServiceProxy paymentTermsServiceProxy;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    void testMode(PaymentTermsServiceInterface targetService, PaymentTermsServiceInterface otherService) {
        testGetPaymentTerms(targetService, otherService);
        testSearchPaymentTermss(targetService, otherService);
    }

    void testGetPaymentTerms(PaymentTermsServiceInterface targetService, PaymentTermsServiceInterface otherService) {
        // Given: a PaymentTermsEntity is returned from the target service
        PaymentTermsEntity entity = PaymentTermsEntity.builder().build();
        when(targetService.getPaymentTerms(anyLong())).thenReturn(entity);

        // When: getPaymentTerms is called on the proxy
        PaymentTermsEntity paymentTermsResult = paymentTermsServiceProxy.getPaymentTerms(1);

        // Then: target service should be used, and the returned paymentTerms should be as expected
        verify(targetService).getPaymentTerms(1);
        verifyNoInteractions(otherService);
        Assertions.assertEquals(entity, paymentTermsResult);
    }

    void testSearchPaymentTermss(PaymentTermsServiceInterface targetService,
                                 PaymentTermsServiceInterface otherService) {
        // Given: a paymentTermss list result is returned from the target service
        PaymentTermsEntity entity = PaymentTermsEntity.builder().build();
        List<PaymentTermsEntity> paymentTermssList = List.of(entity);
        when(targetService.searchPaymentTerms(any())).thenReturn(paymentTermssList);

        // When: searchPaymentTermss is called on the proxy
        PaymentTermsSearchDto criteria = PaymentTermsSearchDto.builder().build();
        List<PaymentTermsEntity> listResult = paymentTermsServiceProxy.searchPaymentTerms(criteria);

        // Then: target service should be used, and the returned list should be as expected
        verify(targetService).searchPaymentTerms(criteria);
        verifyNoInteractions(otherService);
        Assertions.assertEquals(paymentTermssList, listResult);
    }

    @Test
    void shouldUseOpalPaymentTermsServiceWhenModeIsNotLegacy() {
        // Given: app mode is set
        setMode(OPAL);
        // Then: the target service is called, but the other service is not
        testMode(opalService, legacyService);
    }

    @Test
    void shouldUseLegacyPaymentTermsServiceWhenModeIsLegacy() {
        // Given: app mode is set
        setMode(LEGACY);
        // Then: the target service is called, but the other service is not
        testMode(legacyService, opalService);
    }
}
