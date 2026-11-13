package uk.gov.hmcts.opal.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.openapitools.jackson.nullable.JsonNullable;
import uk.gov.hmcts.opal.entity.TillSummaryEntity;
import uk.gov.hmcts.opal.generated.model.TillsItem;

@Mapper(componentModel = "spring")
public interface TillMapper {

    @Mapping(target = "interfaceJobId", expression = "java(toInterfaceJobId(entity))")
    TillsItem toResponse(TillSummaryEntity entity);

    default JsonNullable<Long> toInterfaceJobId(TillSummaryEntity entity) {
        return JsonNullable.of(Boolean.TRUE.equals(entity.getAutoPayment()) ? entity.getInterfaceJobId() : null);
    }
}
