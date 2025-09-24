package uk.gov.hmcts.opal.mapper;

import uk.gov.hmcts.opal.dto.reference.ResultReferenceData;
import uk.gov.hmcts.opal.dto.reference.ResultReferenceDataResponse;
import uk.gov.hmcts.opal.entity.result.ResultEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ResultMapper {

    ResultReferenceData toRefData(ResultEntity.Lite entity);

    default ResultReferenceDataResponse toReferenceDataResponse(List<ResultEntity.Lite> entities) {
        List<ResultReferenceData> dtoList = entities.stream()
            .map(this::toRefData)
            .toList();

        return ResultReferenceDataResponse.builder()
            .refData(dtoList)
            .build();
    }
}
