package uk.gov.hmcts.opal.mapper;

import org.mapstruct.Mapper;
import uk.gov.hmcts.opal.dto.ResultDto;
import uk.gov.hmcts.opal.dto.reference.ResultReferenceData;
import uk.gov.hmcts.opal.dto.reference.ResultReferenceDataResponse;
import uk.gov.hmcts.opal.entity.result.ResultEntity;

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

    ResultDto toDto(ResultEntity entity);
}
