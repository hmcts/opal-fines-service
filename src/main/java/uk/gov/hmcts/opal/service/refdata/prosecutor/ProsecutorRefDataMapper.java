package uk.gov.hmcts.opal.service.refdata.prosecutor;

import static uk.gov.hmcts.opal.service.refdata.prosecutor.ProsecutorRecord.PROSECUTOR_ADDRESS_TYPE;

import java.util.Objects;
import java.util.function.Function;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import uk.gov.hmcts.opal.entity.ProsecutorOpalEntity;
import uk.gov.hmcts.opal.service.refdata.common.Address;

@Mapper(componentModel = "spring")
public interface ProsecutorRefDataMapper {

    @Mapping(target = "prosecutorCode", ignore = true)
    @Mapping(target = "endDate", ignore = true)
    @Mapping(target = "addressLine1",
        expression = "java(prosecutorAddressLine(dto, address -> address.getAddressLine1()))")
    @Mapping(target = "addressLine2",
        expression = "java(prosecutorAddressLine(dto, address -> address.getAddressLine2()))")
    @Mapping(target = "addressLine3",
        expression = "java(prosecutorAddressLine(dto, address -> address.getAddressLine3()))")
    @Mapping(target = "addressLine4",
        expression = "java(prosecutorAddressLine(dto, address -> address.getAddressLine4()))")
    @Mapping(target = "addressLine5",
        expression = "java(prosecutorAddressLine(dto, address -> address.getAddressLine5()))")
    @Mapping(target = "postcode", expression = "java(prosecutorAddressLine(dto, address -> address.getPostCode()))")
    void updateEntityFromDto(ProsecutorRecord dto, @MappingTarget ProsecutorOpalEntity entity);

    default Address prosecutorAddress(ProsecutorRecord dto) {
        if (dto == null || dto.getAddresses() == null || dto.getAddresses().isEmpty()) {
            return null;
        }
        return dto.getAddresses().stream()
            .filter(Objects::nonNull)
            .filter(address -> PROSECUTOR_ADDRESS_TYPE.equals(address.getAddressType()))
            .findFirst()
            .orElse(null);
    }

    default String prosecutorAddressLine(ProsecutorRecord dto, Function<Address, String> getter) {
        Address address = prosecutorAddress(dto);
        return address == null ? null : getter.apply(address);
    }
}
