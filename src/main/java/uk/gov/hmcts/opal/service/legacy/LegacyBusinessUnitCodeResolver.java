package uk.gov.hmcts.opal.service.legacy;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import uk.gov.hmcts.opal.repository.BusinessUnitRepository;

@Service
@RequiredArgsConstructor
public class LegacyBusinessUnitCodeResolver {

    private final BusinessUnitRepository businessUnitRepository;

    public String resolve(String businessUnitId, String legacyBusinessUnitCode) {
        if (StringUtils.hasText(legacyBusinessUnitCode) && !legacyBusinessUnitCode.equals(businessUnitId)) {
            return legacyBusinessUnitCode;
        }

        if (!StringUtils.hasText(businessUnitId)) {
            return legacyBusinessUnitCode;
        }

        try {
            return businessUnitRepository.findById(Short.valueOf(businessUnitId))
                .map(entity -> entity.getBusinessUnitCode())
                .filter(StringUtils::hasText)
                .orElse(legacyBusinessUnitCode);
        } catch (NumberFormatException ignored) {
            return legacyBusinessUnitCode;
        }
    }
}
