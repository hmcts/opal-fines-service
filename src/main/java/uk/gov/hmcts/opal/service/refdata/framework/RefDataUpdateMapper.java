package uk.gov.hmcts.opal.service.refdata.framework;

import org.mapstruct.MappingTarget;

// Implemantations should MapStruct 'ignore' any fields that we don't want to by updatable here,
// especially the primary key
public interface RefDataUpdateMapper<T, E> {

    void updateEntityFromDto(T dto, @MappingTarget E entity);
}
