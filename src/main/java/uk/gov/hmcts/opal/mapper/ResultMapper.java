package uk.gov.hmcts.opal.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.openapitools.jackson.nullable.JsonNullable;
import uk.gov.hmcts.opal.dto.reference.ResultReferenceData;
import uk.gov.hmcts.opal.entity.result.ImpositionCreditor;
import uk.gov.hmcts.opal.entity.result.ResultEntity;
import uk.gov.hmcts.opal.entity.result.ResultType;
import uk.gov.hmcts.opal.generated.model.GetResultByIdResponseResults;
import uk.gov.hmcts.opal.generated.model.GetResultsResponseResults;
import uk.gov.hmcts.opal.generated.model.ResultReferenceDataResults;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ResultMapper {

    ResultReferenceData toRefData(ResultEntity entity);

    default GetResultsResponseResults toReferenceDataResponse(List<ResultEntity> entities) {
        List<ResultReferenceDataResults> dtoList = entities.stream()
            .map(this::toResultReferenceData)
            .toList();

        return GetResultsResponseResults.builder()
            .refData(dtoList)
            .count(dtoList.size())
            .build();
    }

    @Mapping(target = "impositionAllocationOrder", source = "impositionAllocationPriority")
    ResultReferenceDataResults toResultReferenceData(ResultEntity entity);

    @Mapping(target = "impositionCategory", source = "impositionCategory.impositionCategory")
    GetResultByIdResponseResults toDto(ResultEntity entity);

    default String map(ResultType resultType) {
        return resultType == null ? null : resultType.getLabel();
    }

    default String map(ImpositionCreditor impositionCreditor) {
        return impositionCreditor == null ? null : impositionCreditor.getLabel();
    }

    default <T> JsonNullable<T> mapToJsonNullable(T value) {
        return JsonNullable.of(value);
    }

    default JsonNullable<String> mapToJsonNullable(ResultType resultType) {
        return JsonNullable.of(map(resultType));
    }

    default JsonNullable<String> mapToJsonNullable(ImpositionCreditor impositionCreditor) {
        return JsonNullable.of(map(impositionCreditor));
    }
}
