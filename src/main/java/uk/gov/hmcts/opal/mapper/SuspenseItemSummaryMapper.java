package uk.gov.hmcts.opal.mapper;

import java.math.BigDecimal;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.ReportingPolicy;
import org.openapitools.jackson.nullable.JsonNullable;
import uk.gov.hmcts.opal.entity.SuspenseItemSummaryEntity;
import uk.gov.hmcts.opal.entity.SuspenseItemType;
import uk.gov.hmcts.opal.generated.model.BusinessUnitSummaryLiteCommon;
import uk.gov.hmcts.opal.generated.model.SuspenseItemSummaryCommonStrict;
import uk.gov.hmcts.opal.generated.model.SuspenseItemTypeCommonStrict;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SuspenseItemSummaryMapper {

    @Mappings({
        @Mapping(target = "suspenseItemId", source = "entity.id"),
        @Mapping(target = "suspenseItemNumber", source = "entity.suspenseItemNumber"),
        @Mapping(target = "suspenseItemType", expression =
            "java(SuspenseItemSummaryMapper.toSuspenseItemType(entity.getSuspenseItemType()))"),
        @Mapping(target = "createdDate", source = "entity.createdDate"),
        @Mapping(target = "balance", expression = "java(SuspenseItemSummaryMapper.toBalance(entity.getBalance()))"),
        @Mapping(target = "businessUnitDetails", expression =
            "java(SuspenseItemSummaryMapper.toBusinessUnitSummaryCommon(entity))"),
    })
    SuspenseItemSummaryCommonStrict toSuspenseItemSummaryCommonStrict(SuspenseItemSummaryEntity entity);

    static SuspenseItemTypeCommonStrict toSuspenseItemType(String suspenseItemTypeString) {
        SuspenseItemType suspenseItemType = SuspenseItemType.valueOf(suspenseItemTypeString);
        return SuspenseItemTypeCommonStrict
            .builder()
            .itemType(suspenseItemType.name())
            .itemTypeDisplayName(suspenseItemType.getDescription())
            .build();
    }

    static JsonNullable<String> toBalance(BigDecimal balance) {
        return JsonNullable.of(String.valueOf(balance));
    }

    static BusinessUnitSummaryLiteCommon toBusinessUnitSummaryCommon(SuspenseItemSummaryEntity entity) {
        return BusinessUnitSummaryLiteCommon
            .builder()
            .businessUnitId((short) entity.getBusinessUnitId().intValue())
            .businessUnitName(entity.getBusinessUnitName())
            .build();
    }

}
