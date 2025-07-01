package uk.gov.hmcts.opal.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import uk.gov.hmcts.opal.dto.reference.OffenceReferenceData;
import uk.gov.hmcts.opal.dto.reference.OffenceSearchData;
import uk.gov.hmcts.opal.entity.offence.OffenceEntity;

@Mapper(componentModel = "spring")
public interface OffenceMapper {
    @Mapping(target = "dateUsedFrom", source = "dateUsedFrom", qualifiedByName = "toOffset")
    @Mapping(target = "dateUsedTo", source = "dateUsedTo", qualifiedByName = "toOffset")
    OffenceReferenceData toRefData(OffenceEntity entity);

    @Mapping(target = "dateUsedFrom", source = "dateUsedFrom", qualifiedByName = "toOffset")
    @Mapping(target = "dateUsedTo", source = "dateUsedTo", qualifiedByName = "toOffset")
    OffenceSearchData toSearchData(OffenceEntity entity);

    @Named("toOffset")
    static OffsetDateTime toOffset(LocalDateTime localDateTime) {
        return localDateTime == null ? null : localDateTime.atOffset(ZoneOffset.UTC);
    }
}
