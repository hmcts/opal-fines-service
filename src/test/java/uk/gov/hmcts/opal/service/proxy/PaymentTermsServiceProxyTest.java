package uk.gov.hmcts.opal.service.proxy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.opal.dto.AppMode;
import uk.gov.hmcts.opal.dto.search.PaymentTermsSearchDto;
import uk.gov.hmcts.opal.entity.PaymentTermsEntity;
import uk.gov.hmcts.opal.service.DynamicConfigService;
import uk.gov.hmcts.opal.service.legacy.LegacyPaymentTermsService;
import uk.gov.hmcts.opal.service.opal.PaymentTermsService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class PaymentTermsServiceProxyTest {

    private AutoCloseable closeable;

    @Mock
    private PaymentTermsService opalPaymentTermsService;

    @Mock
    private LegacyPaymentTermsService legacyPaymentTermsService;

    @Mock
    private DynamicConfigService dynamicConfigService;

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

    @Test
    void shouldUseOpalPaymentTermsServiceWhenModeIsNotLegacy() {
        // Given: a PaymentTermsEntity and the app mode is set to "opal"
        PaymentTermsEntity entity = PaymentTermsEntity.builder().build();
        AppMode appMode = AppMode.builder().mode("opal").build();
        when(dynamicConfigService.getAppMode()).thenReturn(appMode);
        when(opalPaymentTermsService.getPaymentTerms(anyLong())).thenReturn(entity);

        // When: savePaymentTerms is called on the proxy
        PaymentTermsEntity paymentTermsResult = paymentTermsServiceProxy.getPaymentTerms(1);

        // Then: opalPaymentTermsService should be used, and the returned paymentTerms should be as expected
        verify(opalPaymentTermsService).getPaymentTerms(1);
        verifyNoInteractions(legacyPaymentTermsService);
        Assertions.assertEquals(entity, paymentTermsResult);

        // Given: a paymentTermss list result and the app mode is set to "opal"
        List<PaymentTermsEntity> paymentTermssList = List.of(entity);
        when(opalPaymentTermsService.searchPaymentTerms(any())).thenReturn(paymentTermssList);

        // When: searchPaymentTermss is called on the proxy
        PaymentTermsSearchDto criteria = PaymentTermsSearchDto.builder().build();
        List<PaymentTermsEntity> listResult = paymentTermsServiceProxy.searchPaymentTerms(criteria);

        // Then: opalPaymentTermsService should be used, and the returned list should be as expected
        verify(opalPaymentTermsService).searchPaymentTerms(criteria);
        verifyNoInteractions(legacyPaymentTermsService);
        Assertions.assertEquals(paymentTermssList, listResult);
    }

    @Test
    void shouldUseLegacyPaymentTermsServiceWhenModeIsLegacy() {
        // Given: a PaymentTermsEntity and the app mode is set to "legacy"
        PaymentTermsEntity entity = PaymentTermsEntity.builder().build();
        AppMode appMode = AppMode.builder().mode("legacy").build();
        when(dynamicConfigService.getAppMode()).thenReturn(appMode);
        when(legacyPaymentTermsService.getPaymentTerms(anyLong())).thenReturn(entity);

        // When: savePaymentTerms is called on the proxy
        PaymentTermsEntity result = paymentTermsServiceProxy.getPaymentTerms(1);

        // Then: legacyPaymentTermsService should be used, and the returned paymentTerms should be as expected
        verify(legacyPaymentTermsService).getPaymentTerms(1);
        verifyNoInteractions(opalPaymentTermsService);
        Assertions.assertEquals(entity, result);

        // Given: a paymentTermss list result and the app mode is set to "legacy"
        List<PaymentTermsEntity> paymentTermssList = List.of(entity);
        when(legacyPaymentTermsService.searchPaymentTerms(any())).thenReturn(paymentTermssList);

        // When: searchPaymentTermss is called on the proxy
        PaymentTermsSearchDto criteria = PaymentTermsSearchDto.builder().build();
        List<PaymentTermsEntity> listResult = paymentTermsServiceProxy.searchPaymentTerms(criteria);

        // Then: opalPaymentTermsService should be used, and the returned list should be as expected
        verify(legacyPaymentTermsService).searchPaymentTerms(criteria);
        verifyNoInteractions(opalPaymentTermsService);
        Assertions.assertEquals(paymentTermssList, listResult); // Not yet implemented in Legacy mode
    }
}
