package uk.gov.hmcts.opal.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.openapitools.jackson.nullable.JsonNullable;
import uk.gov.hmcts.opal.entity.SuspenseItemSummaryEntity;
import uk.gov.hmcts.opal.generated.model.BusinessUnitSummaryLiteCommon;
import uk.gov.hmcts.opal.generated.model.SuspenseItemSummaryCommonStrict;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SuspenseItemSummaryMapper {

    @Mapping(target = "suspenseItemId", source = "entity.id")
    @Mapping(target = "suspenseItemNumber", source = "entity.suspenseItemNumber")
    @Mapping(target = "suspenseItemType", expression =
        "java(SuspenseItemSummaryMapper.toSuspenseItemType(entity.getSuspenseItemType()))")
    @Mapping(target = "createdDate", source = "entity.createdDate")
    @Mapping(target = "balance", expression = "java(SuspenseItemSummaryMapper.toBalance(entity.getBalance()))")
    @Mapping(target = "businessUnitDetails", expression =
        "java(SuspenseItemSummaryMapper.toBusinessUnitSummaryCommon(entity))")
    SuspenseItemSummaryCommonStrict toSuspenseItemSummaryCommonStrict(SuspenseItemSummaryEntity entity);

    static JsonNullable<String> toSuspenseItemType(String suspenseItemType) {
        return JsonNullable.of(suspenseItemType);
    }

    static JsonNullable<String> toBalance(Double balance) {
        return JsonNullable.of(String.valueOf(balance));
    }

    static BusinessUnitSummaryLiteCommon toBusinessUnitSummaryCommon(SuspenseItemSummaryEntity entity) {
        return BusinessUnitSummaryLiteCommon
            .builder()
            .businessUnitId(entity.getBusinessUnitId())
            .businessUnitName(entity.getBusinessUnitName())
            .build();
    }

}
