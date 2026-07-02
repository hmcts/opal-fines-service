package uk.gov.hmcts.opal.service.opal;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.entity.MappingValue;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountStatus;
import uk.gov.hmcts.opal.generated.model.MappingItemMappings;

@Service
@Qualifier("mappingsService")
@Slf4j(topic = "opal.MappingsService")
public class MappingsService {

    private static final String DEFENDANT_ACCOUNT_STATUS_TYPE = "defendant-account-status";

    private static final Map<String, EnumMappingSource<?>> SUPPORTED_MAPPINGS = Map.of(
        DEFENDANT_ACCOUNT_STATUS_TYPE, new EnumMappingSource<>(DefendantAccountStatus.class)
    );

    public List<MappingItemMappings> getMappings(String type) {
        EnumMappingSource<?> mappingSource = SUPPORTED_MAPPINGS.get(type);

        if (mappingSource == null) {
            throw new NoSuchElementException("Unsupported mapping type: " + type);
        }

        log.debug(":getMappings: type: {}", type);
        return mappingSource.getValues();
    }

    private record EnumMappingSource<T extends Enum<T> & MappingValue>(Class<T> enumClass) {

        private List<MappingItemMappings> getValues() {
            return Arrays.stream(enumClass.getEnumConstants())
                .map(value -> MappingItemMappings.builder()
                    .code(value.getCode())
                    .displayName(value.getDisplayName())
                    .build())
                .toList();
        }
    }
}
