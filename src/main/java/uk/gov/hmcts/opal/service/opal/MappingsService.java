package uk.gov.hmcts.opal.service.opal;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.dto.reference.MappingItem;
import uk.gov.hmcts.opal.entity.MappingValue;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountStatus;

@Service
@Qualifier("mappingsService")
@Slf4j(topic = "opal.MappingsService")
public class MappingsService {

    private static final String DEFENDANT_ACCOUNT_STATUS_TYPE = "defendant-account-status";

    private static final Map<String, EnumMappingSource<?>> SUPPORTED_MAPPINGS = Map.of(
        DEFENDANT_ACCOUNT_STATUS_TYPE, new EnumMappingSource<>(DefendantAccountStatus.class)
    );

    @Transactional(readOnly = true)
    public List<MappingItem> getMappings(String type) {
        EnumMappingSource<?> mappingSource = SUPPORTED_MAPPINGS.get(type);

        if (mappingSource == null) {
            throw new NoSuchElementException("Unsupported mapping type: " + type);
        }

        log.debug(":getMappings: type: {}", type);
        return mappingSource.getValues();
    }

    private record EnumMappingSource<T extends Enum<T> & MappingValue>(Class<T> enumClass) {

        private List<MappingItem> getValues() {
            return Arrays.stream(enumClass.getEnumConstants())
                .map(value -> new MappingItem(value.getCode(), value.getDisplayName()))
                .toList();
        }
    }
}
