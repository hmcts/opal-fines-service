package uk.gov.hmcts.opal.service.opal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.common.launchdarkly.FeatureDisabledException;
import uk.gov.hmcts.opal.common.launchdarkly.service.FeatureToggleApi;
import uk.gov.hmcts.opal.entity.MappingValue;
import uk.gov.hmcts.opal.entity.MethodOfPayment;
import uk.gov.hmcts.opal.entity.Source;
import uk.gov.hmcts.opal.entity.SuspenseReason;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountStatus;
import uk.gov.hmcts.opal.exception.UnsupportedMappingTypeException;
import uk.gov.hmcts.opal.generated.model.MappingItemMappings;
import uk.gov.hmcts.opal.service.report.FileType;
import uk.gov.hmcts.opal.util.FeatureFlags;

@Service
@Qualifier("mappingsService")
@RequiredArgsConstructor
@Slf4j(topic = "opal.MappingsService")
public class MappingsService {

    private static final String DEFENDANT_ACCOUNT_STATUS_TYPE = "defendant-account-status";
    private static final String FILE_TYPE = "file-type";
    private static final String METHOD_OF_PAYMENT = "method-of-payment";
    private static final String SOURCE = "source";
    private static final String SUSPENSE_REASON = "suspense-reason";
    private static final Set<String> RELEASE_1B_MAPPING_TYPES = Set.of(DEFENDANT_ACCOUNT_STATUS_TYPE, FILE_TYPE);
    private static final Set<String>
        RELEASE_1C_PAYMENT_MAPPING_TYPES = Set.of(METHOD_OF_PAYMENT, SOURCE, SUSPENSE_REASON);

    private static final Map<String, EnumMappingSource<?>> SUPPORTED_MAPPINGS = Map.of(
        DEFENDANT_ACCOUNT_STATUS_TYPE, new EnumMappingSource<>(DefendantAccountStatus.class),
        FILE_TYPE, new EnumMappingSource<>(FileType.class),
        METHOD_OF_PAYMENT, new EnumMappingSource<>(MethodOfPayment.class),
        SOURCE, new EnumMappingSource<>(Source.class),
        SUSPENSE_REASON, new EnumMappingSource<>(SuspenseReason.class)
    );

    private final FeatureToggleApi featureToggleApi;

    public List<MappingItemMappings> getMappings(String type) {
        EnumMappingSource<?> mappingSource = SUPPORTED_MAPPINGS.get(type);

        if (mappingSource == null) {
            throw new UnsupportedMappingTypeException(type, getMappingTypesOrThrowIfNoneEnabled());
        }

        requireMappingTypeEnabled(type);
        log.debug(":getMappings: type: {}", type);
        return mappingSource.getValues();
    }

    public List<String> getSupportedMappingTypes() {
        List<String> supportedTypes = new ArrayList<>();
        if (isRelease1bEnabled()) {
            supportedTypes.addAll(RELEASE_1B_MAPPING_TYPES);
        }
        if (isRelease1cPaymentEnabled()) {
            supportedTypes.addAll(RELEASE_1C_PAYMENT_MAPPING_TYPES);
        }

        return supportedTypes.stream().sorted().toList();
    }

    public List<String> getMappingTypesOrThrowIfNoneEnabled() {
        List<String> supportedTypes = getSupportedMappingTypes();
        if (supportedTypes.isEmpty()) {
            throw new FeatureDisabledException("No mapping types are currently enabled");
        }
        return supportedTypes;
    }

    private void requireMappingTypeEnabled(String type) {
        boolean isPaymentType = RELEASE_1C_PAYMENT_MAPPING_TYPES.contains(type);
        String featureFlag = isPaymentType ? FeatureFlags.RELEASE_1C_PAYMENT : FeatureFlags.RELEASE_1B;
        boolean enabled = isPaymentType ? isRelease1cPaymentEnabled() : isRelease1bEnabled();

        if (!enabled) {
            throw new FeatureDisabledException("Feature " + featureFlag + " is not enabled for mappings");
        }
    }

    private boolean isRelease1cPaymentEnabled() {
        return featureToggleApi.isFeatureEnabledWithPropertyValueDefault(
            FeatureFlags.RELEASE_1C_PAYMENT, FeatureFlags.RELEASE_1C_PAYMENT_ENABLED_PROPERTY, false);
    }

    private boolean isRelease1bEnabled() {
        return featureToggleApi.isFeatureEnabledWithPropertyValueDefault(
            FeatureFlags.RELEASE_1B, FeatureFlags.RELEASE_1B_ENABLED_PROPERTY, false);
    }

    private record EnumMappingSource<T extends Enum<T> & MappingValue>(Class<T> enumClass) {

        private List<MappingItemMappings> getValues() {
            return Arrays.stream(enumClass.getEnumConstants())
                .map(this::toMappingItemMappings)
                .toList();
        }

        private MappingItemMappings toMappingItemMappings(T value) {
            MappingItemMappings mapping = MappingItemMappings.builder()
                .code(value.getCode())
                .displayName(value.getDisplayName())
                .build();

            if (value instanceof FileType fileType) {
                mapping.mimeType(fileType.getMimeType());
            }
            return mapping;
        }
    }
}
