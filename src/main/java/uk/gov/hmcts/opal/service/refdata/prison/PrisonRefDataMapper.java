package uk.gov.hmcts.opal.service.refdata.prison;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import uk.gov.hmcts.opal.entity.PrisonEntity;
import uk.gov.hmcts.opal.service.refdata.common.Address;

@Mapper(componentModel = "spring")
public interface PrisonRefDataMapper {

    default void updateEntityFromDto(PrisonRecord dto, PrisonEntity entity) {
        updateEntity(dto, dto.prisonAddress().orElseGet(Address::new), entity);
    }

    @Mapping(target = "prisonId", ignore = true)
    @Mapping(target = "name", source = "dto.name")
    @Mapping(target = "addressLine1", source = "address.addressLine1")
    @Mapping(target = "addressLine2", source = "address.addressLine2")
    @Mapping(target = "addressLine3", source = "address.addressLine3")
    @Mapping(target = "addressLine4", source = "address.addressLine4")
    @Mapping(target = "addressLine5", source = "address.addressLine5")
    @Mapping(target = "postcode", source = "address.postCode")
    void updateEntity(PrisonRecord dto, Address address, @MappingTarget PrisonEntity entity);
}
