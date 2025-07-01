package uk.gov.hmcts.opal.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uk.gov.hmcts.opal.dto.reference.OffenceReferenceData;
import uk.gov.hmcts.opal.dto.reference.OffenceSearchData;
import uk.gov.hmcts.opal.entity.offence.OffenceEntity;

@Mapper(componentModel = "spring")
public interface OffenceMapper {
    @Mapping(
        target = "dateUsedFrom",
        expression = "java(uk.gov.hmcts.opal.util.DateTimeUtils.toUtcDateTime(entity.getDateUsedFrom()))"
    )
    @Mapping(
        target = "dateUsedTo",
        expression = "java(uk.gov.hmcts.opal.util.DateTimeUtils.toUtcDateTime(entity.getDateUsedTo()))"
    )
    OffenceReferenceData toRefData(OffenceEntity entity);

    @Mapping(
        target = "dateUsedFrom",
        expression = "java(uk.gov.hmcts.opal.util.DateTimeUtils.toUtcDateTime(entity.getDateUsedFrom()))"
    )
    @Mapping(
        target = "dateUsedTo",
        expression = "java(uk.gov.hmcts.opal.util.DateTimeUtils.toUtcDateTime(entity.getDateUsedTo()))"
    )
    OffenceSearchData toSearchData(OffenceEntity entity);
}
