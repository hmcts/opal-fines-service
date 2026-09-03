package uk.gov.hmcts.opal.service.refdata.lja;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import uk.gov.hmcts.opal.entity.LocalJusticeAreaEntity;
import uk.gov.hmcts.opal.service.refdata.lja.LjaRecord.Address;
import uk.gov.hmcts.opal.service.refdata.framework.RefDataUpdateMapper;

@Mapper(componentModel = "spring")
public interface LocalJusticeAreaMapper
    extends RefDataUpdateMapper<LjaRecord, LocalJusticeAreaEntity> {

    @Override
    @Mapping(target = "localJusticeAreaId", ignore = true)
    @Mapping(target = "ljaType", constant = "LJA")
    @Mapping(target = "name", source = "ljaName")
    @Mapping(target = "addressLine1", expression = "java(firstAddressLine1(dto))")
    @Mapping(target = "addressLine2", expression = "java(firstAddressLine2(dto))")
    @Mapping(target = "addressLine3", expression = "java(firstAddressLine3(dto))")
    @Mapping(target = "addressLine4", expression = "java(firstAddressLine4(dto))")
    @Mapping(target = "postcode", expression = "java(firstPostcode(dto))")
    @Mapping(target = "addressLine5", expression = "java(null)")
    void updateEntityFromDto(LjaRecord dto, @MappingTarget LocalJusticeAreaEntity entity);

    default LocalDateTime toLocalDateTime(LocalDate endDate) {
        return endDate == null ? null : endDate.atStartOfDay();
    }

    default Address firstAddress(LjaRecord dto) {
        if (dto == null || dto.getAddresses() == null || dto.getAddresses().isEmpty()) {
            return null;
        }
        return dto.getAddresses().get(0);
    }

    default String firstAddressLine1(LjaRecord dto) {
        Address address = firstAddress(dto);
        return address == null ? null : address.getAddressLine1();
    }

    default String firstAddressLine2(LjaRecord dto) {
        Address address = firstAddress(dto);
        return address == null ? null : address.getAddressLine2();
    }

    default String firstAddressLine3(LjaRecord dto) {
        Address address = firstAddress(dto);
        return address == null ? null : address.getAddressLine3();
    }

    default String firstAddressLine4(LjaRecord dto) {
        Address address = firstAddress(dto);
        return address == null ? null : address.getAddressLine4();
    }

    default String firstPostcode(LjaRecord dto) {
        Address address = firstAddress(dto);
        return address == null ? null : address.getPostcode();
    }
}
