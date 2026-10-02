package uk.gov.hmcts.opal.service.opal;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.entity.MappingValue;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountStatus;
import uk.gov.hmcts.opal.exception.UnsupportedMappingTypeException;
import uk.gov.hmcts.opal.generated.model.MappingItemMappings;
import uk.gov.hmcts.opal.service.report.FileType;

@Service
@Qualifier("mappingsService")
@Slf4j(topic = "opal.MappingsService")
public class MappingsService {

    private static final String DEFENDANT_ACCOUNT_STATUS_TYPE = "defendant-account-status";
    private static final String FILE_TYPE = "file-type";

    private static final Map<String, EnumMappingSource<?>> SUPPORTED_MAPPINGS = Map.of(
        DEFENDANT_ACCOUNT_STATUS_TYPE, new EnumMappingSource<>(DefendantAccountStatus.class),
        FILE_TYPE, new EnumMappingSource<>(FileType.class)
    );

    private static final List<String> SUPPORTED_MAPPING_TYPES = SUPPORTED_MAPPINGS.keySet()
        .stream()
        .sorted()
        .toList();

    public List<MappingItemMappings> getMappings(String type) {
        EnumMappingSource<?> mappingSource = SUPPORTED_MAPPINGS.get(type);

        if (mappingSource == null) {
            throw new UnsupportedMappingTypeException(type, SUPPORTED_MAPPING_TYPES);
        }

        log.debug(":getMappings: type: {}", type);
        return mappingSource.getValues();
    }

    public List<String> getSupportedMappingTypes() {
        return SUPPORTED_MAPPING_TYPES;
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
