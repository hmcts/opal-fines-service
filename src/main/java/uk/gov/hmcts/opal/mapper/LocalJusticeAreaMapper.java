package uk.gov.hmcts.opal.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.opal.dto.LocalJusticeAreaDto;
import uk.gov.hmcts.opal.entity.LocalJusticeAreaEntity;


@Component
public class LocalJusticeAreaMapper {

    public LocalJusticeAreaDto toDto(LocalJusticeAreaEntity entity) {

        if (entity == null) {
            return null;
        }

        return LocalJusticeAreaDto.builder()
            .localJusticeAreaId(entity.getLocalJusticeAreaId())
            .ljaCode(entity.getLjaCode())
            .ljaType(entity.getLjaType())
            .addressLine1(entity.getAddressLine1())
            .addressLine2(entity.getAddressLine2())
            .addressLine3(entity.getAddressLine3())
            .addressLine4(entity.getAddressLine4())
            .addressLine5(entity.getAddressLine5())
            .postcode(entity.getPostcode())
            .endDate(entity.getEndDate())
            .name(entity.getName())
            .build();
    }
}