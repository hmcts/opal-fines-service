package uk.gov.hmcts.opal.service.refdata.lja;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import uk.gov.hmcts.opal.entity.LocalJusticeAreaEntity;
import uk.gov.hmcts.opal.generated.refdata.LocalJusticeAreaUpdateDto;
import uk.gov.hmcts.opal.service.refdata.framework.RefDataUpdateMapper;

@Mapper(componentModel = "spring")
public interface LocalJusticeAreaMapper
    extends RefDataUpdateMapper<LocalJusticeAreaUpdateDto, LocalJusticeAreaEntity> {

    @Override
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "localJusticeAreaId", ignore = true)
    void updateEntityFromDto(LocalJusticeAreaUpdateDto dto,
        @MappingTarget LocalJusticeAreaEntity entity);
}
