package uk.gov.hmcts.opal.mapper;

import uk.gov.hmcts.opal.dto.reference.ResultReferenceData;
import uk.gov.hmcts.opal.dto.reference.ResultReferenceDataResponse;
import uk.gov.hmcts.opal.entity.result.ResultEntityFull;
import uk.gov.hmcts.opal.entity.result.ResultEntityLite;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ResultMapper {

    ResultReferenceData toRefData(ResultEntityLite entity);

    ResultReferenceData toRefDataFromFull(ResultEntityFull entity);

    default ResultReferenceDataResponse toReferenceDataResponse(List<ResultEntityLite> entities) {
        List<ResultReferenceData> dtoList = entities.stream()
            .map(this::toRefData)
            .toList();

        return ResultReferenceDataResponse.builder()
            .refData(dtoList)
            .build();
    }
}
