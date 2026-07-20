package uk.gov.hmcts.opal.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uk.gov.hmcts.opal.dto.ResultDto;
import uk.gov.hmcts.opal.dto.reference.ResultReferenceData;
import uk.gov.hmcts.opal.dto.reference.ResultReferenceDataResponse;
import uk.gov.hmcts.opal.entity.result.ImpositionCreditor;
import uk.gov.hmcts.opal.entity.result.ResultEntity;
import uk.gov.hmcts.opal.entity.result.ResultType;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ResultMapper {

    ResultReferenceData toRefData(ResultEntity entity);

    default ResultReferenceDataResponse toReferenceDataResponse(List<ResultEntity> entities) {
        List<ResultReferenceData> dtoList = entities.stream()
            .map(this::toRefData)
            .toList();

        return ResultReferenceDataResponse.builder()
            .refData(dtoList)
            .build();
    }

    @Mapping(target = "impositionCategory", source = "impositionCategory.impositionCategory")
    ResultDto toDto(ResultEntity entity);

    default String map(ResultType resultType) {
        return resultType == null ? null : resultType.getLabel();
    }

    default String map(ImpositionCreditor impositionCreditor) {
        return impositionCreditor == null ? null : impositionCreditor.getLabel();
    }
}
