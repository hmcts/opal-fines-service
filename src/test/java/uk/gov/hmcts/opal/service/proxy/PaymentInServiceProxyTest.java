package uk.gov.hmcts.opal.service.proxy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.opal.dto.AppMode;
import uk.gov.hmcts.opal.dto.search.PaymentInSearchDto;
import uk.gov.hmcts.opal.entity.PaymentInEntity;
import uk.gov.hmcts.opal.service.DynamicConfigService;
import uk.gov.hmcts.opal.service.legacy.LegacyPaymentInService;
import uk.gov.hmcts.opal.service.opal.PaymentInService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class PaymentInServiceProxyTest {

    private AutoCloseable closeable;

    @Mock
    private PaymentInService opalPaymentInService;

    @Mock
    private LegacyPaymentInService legacyPaymentInService;

    @Mock
    private DynamicConfigService dynamicConfigService;

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

    @Test
    void shouldUseOpalPaymentInServiceWhenModeIsNotLegacy() {
        // Given: a PaymentInEntity and the app mode is set to "opal"
        PaymentInEntity entity = PaymentInEntity.builder().build();
        AppMode appMode = AppMode.builder().mode("opal").build();
        when(dynamicConfigService.getAppMode()).thenReturn(appMode);
        when(opalPaymentInService.getPaymentIn(anyLong())).thenReturn(entity);

        // When: savePaymentIn is called on the proxy
        PaymentInEntity paymentInResult = paymentInServiceProxy.getPaymentIn(1);

        // Then: opalPaymentInService should be used, and the returned paymentIn should be as expected
        verify(opalPaymentInService).getPaymentIn(1);
        verifyNoInteractions(legacyPaymentInService);
        Assertions.assertEquals(entity, paymentInResult);

        // Given: a paymentIns list result and the app mode is set to "opal"
        List<PaymentInEntity> paymentInsList = List.of(entity);
        when(opalPaymentInService.searchPaymentIns(any())).thenReturn(paymentInsList);

        // When: searchPaymentIns is called on the proxy
        PaymentInSearchDto criteria = PaymentInSearchDto.builder().build();
        List<PaymentInEntity> listResult = paymentInServiceProxy.searchPaymentIns(criteria);

        // Then: opalPaymentInService should be used, and the returned list should be as expected
        verify(opalPaymentInService).searchPaymentIns(criteria);
        verifyNoInteractions(legacyPaymentInService);
        Assertions.assertEquals(paymentInsList, listResult);
    }

    @Test
    void shouldUseLegacyPaymentInServiceWhenModeIsLegacy() {
        // Given: a PaymentInEntity and the app mode is set to "legacy"
        PaymentInEntity entity = PaymentInEntity.builder().build();
        AppMode appMode = AppMode.builder().mode("legacy").build();
        when(dynamicConfigService.getAppMode()).thenReturn(appMode);
        when(legacyPaymentInService.getPaymentIn(anyLong())).thenReturn(entity);

        // When: savePaymentIn is called on the proxy
        PaymentInEntity result = paymentInServiceProxy.getPaymentIn(1);

        // Then: legacyPaymentInService should be used, and the returned paymentIn should be as expected
        verify(legacyPaymentInService).getPaymentIn(1);
        verifyNoInteractions(opalPaymentInService);
        Assertions.assertEquals(entity, result);

        // Given: a paymentIns list result and the app mode is set to "legacy"
        List<PaymentInEntity> paymentInsList = List.of(entity);
        when(legacyPaymentInService.searchPaymentIns(any())).thenReturn(paymentInsList);

        // When: searchPaymentIns is called on the proxy
        PaymentInSearchDto criteria = PaymentInSearchDto.builder().build();
        List<PaymentInEntity> listResult = paymentInServiceProxy.searchPaymentIns(criteria);

        // Then: opalPaymentInService should be used, and the returned list should be as expected
        verify(legacyPaymentInService).searchPaymentIns(criteria);
        verifyNoInteractions(opalPaymentInService);
        Assertions.assertEquals(paymentInsList, listResult); // Not yet implemented in Legacy mode
    }
}
