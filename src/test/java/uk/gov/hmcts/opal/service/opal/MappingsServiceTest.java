package uk.gov.hmcts.opal.service.opal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.common.launchdarkly.FeatureDisabledException;
import uk.gov.hmcts.opal.common.launchdarkly.service.FeatureToggleApi;
import uk.gov.hmcts.opal.exception.UnsupportedMappingTypeException;
import uk.gov.hmcts.opal.generated.model.MappingItemMappings;
import uk.gov.hmcts.opal.util.FeatureFlags;

@ExtendWith(MockitoExtension.class)
class MappingsServiceTest {

    @Mock
    private FeatureToggleApi featureToggleApi;

    @InjectMocks
    private MappingsService mappingsService;

    @Test
    void getMappings_returnsSupportedDefendantAccountStatusMappings() {
        enableRelease1b();
        List<MappingItemMappings> mappings = mappingsService.getMappings("defendant-account-status");

        assertEquals(List.of(
            MappingItemMappings.builder().code("CS").displayName("Account consolidated").build(),
            MappingItemMappings.builder().code("L").displayName("Live").build(),
            MappingItemMappings.builder().code("TA").displayName("TFO acknowledged").build(),
            MappingItemMappings.builder().code("TO").displayName("TFO to be acknowledged").build(),
            MappingItemMappings.builder().code("TS").displayName("TFO to NI/Scotland to be acknowledged").build(),
            MappingItemMappings.builder().code("WO").displayName("Account written off").build()
        ), mappings);
    }

    @Test
    void getMappings_throwsWhenTypeIsNotAllowListed() {
        enableRelease1b();
        enableRelease1cPayment();
        UnsupportedMappingTypeException exception = assertThrows(UnsupportedMappingTypeException.class,
            () -> mappingsService.getMappings("unsupported-type"));

        assertEquals(
            "Unsupported mapping type: unsupported-type. Supported types: defendant-account-status, file-type, "
                + "method-of-payment, source, suspense-reason",
            exception.getMessage()
        );
        assertEquals(List.of("defendant-account-status", "file-type", "method-of-payment", "source",
            "suspense-reason"), exception.getSupportedTypes());
    }

    @Test
    void getMappings_omitsPaymentTypesFromUnsupportedTypeErrorWhenRelease1cPaymentIsDisabled() {
        enableRelease1b();
        disableRelease1cPayment();

        UnsupportedMappingTypeException exception = assertThrows(UnsupportedMappingTypeException.class,
            () -> mappingsService.getMappings("unsupported-type"));

        assertEquals("Unsupported mapping type: unsupported-type. Supported types: defendant-account-status, "
            + "file-type", exception.getMessage());
        assertEquals(List.of("defendant-account-status", "file-type"), exception.getSupportedTypes());
    }

    @Test
    void getMappings_omitsRelease1bTypesFromUnsupportedTypeErrorWhenRelease1bIsDisabled() {
        disableRelease1b();
        enableRelease1cPayment();

        UnsupportedMappingTypeException exception = assertThrows(UnsupportedMappingTypeException.class,
            () -> mappingsService.getMappings("unsupported-type"));

        assertEquals("Unsupported mapping type: unsupported-type. Supported types: method-of-payment, source, "
            + "suspense-reason", exception.getMessage());
        assertEquals(List.of("method-of-payment", "source", "suspense-reason"), exception.getSupportedTypes());
    }

    @Test
    void getSupportedMappingTypes_includesAllTypesWhenBothFlagsAreEnabled() {
        enableRelease1b();
        enableRelease1cPayment();

        assertEquals(List.of("defendant-account-status", "file-type", "method-of-payment", "source",
            "suspense-reason"), mappingsService.getSupportedMappingTypes());
    }

    @Test
    void getSupportedMappingTypes_includesExistingTypesWhenOnlyRelease1bIsEnabled() {
        enableRelease1b();
        disableRelease1cPayment();

        assertEquals(List.of("defendant-account-status", "file-type"), mappingsService.getSupportedMappingTypes());
    }

    @Test
    void getSupportedMappingTypes_includesPaymentTypesWhenOnlyRelease1cPaymentIsEnabled() {
        disableRelease1b();
        enableRelease1cPayment();

        assertEquals(List.of("method-of-payment", "source", "suspense-reason"),
            mappingsService.getSupportedMappingTypes());
    }

    @Test
    void getSupportedMappingTypes_returnsEmptyListWhenBothFlagsAreDisabled() {
        disableRelease1b();
        disableRelease1cPayment();

        assertEquals(List.of(), mappingsService.getSupportedMappingTypes());
    }

    @Test
    void getMappings_returnsSupportedFileTypeMappings() {
        enableRelease1b();
        List<MappingItemMappings> mappings = mappingsService.getMappings("file-type");

        assertEquals(List.of(
            MappingItemMappings.builder().code("CSV").displayName("CSV").mimeType("text/csv").build(),
            MappingItemMappings.builder().code("PDF").displayName("PDF").mimeType("application/pdf").build(),
            MappingItemMappings.builder().code("JSON").displayName("JSON").mimeType("application/json").build(),
            MappingItemMappings.builder().code("XML").displayName("XML").mimeType("application/xml").build()
        ), mappings);
    }

    @Test
    void getMappings_returnsMethodOfPaymentMappings() {
        enableRelease1cPayment();
        List<MappingItemMappings> mappings = mappingsService.getMappings("method-of-payment");

        assertEquals(List.of(
            MappingItemMappings.builder().code("NC").displayName("Notes and coins").build(),
            MappingItemMappings.builder().code("PO").displayName("Postal order").build(),
            MappingItemMappings.builder().code("CQ").displayName("Cheque").build(),
            MappingItemMappings.builder().code("CT").displayName("Credit transfer").build()
        ), mappings);
    }

    @Test
    void getMappings_returnsSourceMappings() {
        enableRelease1cPayment();
        List<MappingItemMappings> mappings = mappingsService.getMappings("source");

        assertEquals(List.of(
            MappingItemMappings.builder().code("ALLPAY").displayName("allpay").build(),
            MappingItemMappings.builder().code("BARCLAYCARD").displayName("Barclaycard").build(),
            MappingItemMappings.builder().code("BTECKOH").displayName("British Telecom").build(),
            MappingItemMappings.builder().code("DWP").displayName("DWP/AEA").build(),
            MappingItemMappings.builder().code("NATWEST").displayName("NatWest").build(),
            MappingItemMappings.builder().code("OTHER").displayName("Other").build()
        ), mappings);
    }

    @Test
    void getMappings_returnsSuspenseReasonMappings() {
        enableRelease1cPayment();
        List<MappingItemMappings> mappings = mappingsService.getMappings("suspense-reason");

        assertEquals(List.of(
            MappingItemMappings.builder().code("OF").displayName("Fines overpayment").build(),
            MappingItemMappings.builder().code("FA").displayName("Fine paid in advance").build(),
            MappingItemMappings.builder().code("MS").displayName("Miscellaneous payment").build(),
            MappingItemMappings.builder().code("UN").displayName("Unidentified Amount").build()
        ), mappings);
    }

    @ParameterizedTest
    @ValueSource(strings = {"method-of-payment", "source", "suspense-reason"})
    void getMappings_rejectsPaymentTypeWhenRelease1cPaymentIsDisabled(String type) {
        FeatureDisabledException exception = assertThrows(FeatureDisabledException.class,
            () -> mappingsService.getMappings(type));

        assertEquals("Feature release-1c-payment is not enabled for mappings", exception.getMessage());
        verify(featureToggleApi).isFeatureEnabledWithPropertyValueDefault(
            FeatureFlags.RELEASE_1C_PAYMENT, FeatureFlags.RELEASE_1C_PAYMENT_ENABLED_PROPERTY, false);
    }

    @ParameterizedTest
    @ValueSource(strings = {"defendant-account-status", "file-type"})
    void getMappings_rejectsOtherTypesWhenRelease1bIsDisabled(String type) {
        FeatureDisabledException exception = assertThrows(FeatureDisabledException.class,
            () -> mappingsService.getMappings(type));

        assertEquals("Feature release-1b is not enabled for mappings", exception.getMessage());
        verify(featureToggleApi).isFeatureEnabledWithPropertyValueDefault(
            FeatureFlags.RELEASE_1B, FeatureFlags.RELEASE_1B_ENABLED_PROPERTY, false);
    }

    @Test
    void getMappings_allowsPaymentTypeWhenRelease1bIsDisabled() {
        enableRelease1cPayment();

        assertEquals("NC", mappingsService.getMappings("method-of-payment").getFirst().getCode());
    }

    private void enableRelease1b() {
        when(featureToggleApi.isFeatureEnabledWithPropertyValueDefault(
            FeatureFlags.RELEASE_1B, FeatureFlags.RELEASE_1B_ENABLED_PROPERTY, false)).thenReturn(true);
    }

    private void disableRelease1b() {
        when(featureToggleApi.isFeatureEnabledWithPropertyValueDefault(
            FeatureFlags.RELEASE_1B, FeatureFlags.RELEASE_1B_ENABLED_PROPERTY, false)).thenReturn(false);
    }

    private void enableRelease1cPayment() {
        when(featureToggleApi.isFeatureEnabledWithPropertyValueDefault(
            FeatureFlags.RELEASE_1C_PAYMENT, FeatureFlags.RELEASE_1C_PAYMENT_ENABLED_PROPERTY, false)).thenReturn(true);
    }

    private void disableRelease1cPayment() {
        when(featureToggleApi.isFeatureEnabledWithPropertyValueDefault(
            FeatureFlags.RELEASE_1C_PAYMENT, FeatureFlags.RELEASE_1C_PAYMENT_ENABLED_PROPERTY,
            false)).thenReturn(false);
    }
}
