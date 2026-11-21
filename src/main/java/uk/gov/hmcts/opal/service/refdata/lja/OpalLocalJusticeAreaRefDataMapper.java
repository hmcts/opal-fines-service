package uk.gov.hmcts.opal.service.refdata.lja;

import static uk.gov.hmcts.opal.service.refdata.lja.OpalLjaRecord.COURT_ADDRESS_TYPE;

import java.util.Objects;
import java.util.function.Function;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import uk.gov.hmcts.opal.entity.OpalLocalJusticeAreaEntity;
import uk.gov.hmcts.opal.entity.OpalLocalJusticeAreaType;
import uk.gov.hmcts.opal.service.refdata.common.Address;

@Mapper(componentModel = "spring")
public interface OpalLocalJusticeAreaRefDataMapper {

    // This is the business key - we don't update this
    @Mapping(target = "localJusticeAreaId", ignore = true)

    // Entity attributes we are choosing not to  update
    @Mapping(target = "endDate", ignore = true)

    // Entity attributes we are choosing to update
    @Mapping(target = "ljaType", expression = "java(toLocalJusticeAreaType(dto.getLjaType()))")
    @Mapping(target = "name", source = "ljaName")
    @Mapping(target = "addressLine1", expression = "java(courtAddressLine(dto, address -> address.getAddressLine1()))")
    @Mapping(target = "addressLine2", expression = "java(courtAddressLine(dto, address -> address.getAddressLine2()))")
    @Mapping(target = "addressLine3", expression = "java(courtAddressLine(dto, address -> address.getAddressLine3()))")
    @Mapping(target = "addressLine4", expression = "java(courtAddressLine(dto, address -> address.getAddressLine4()))")
    @Mapping(target = "addressLine5", expression = "java(courtAddressLine(dto, address -> address.getAddressLine5()))")
    @Mapping(target = "postcode", expression = "java(courtAddressLine(dto, address -> address.getPostCode()))")


    void updateEntityFromDto(OpalLjaRecord dto, @MappingTarget OpalLocalJusticeAreaEntity entity);

    default OpalLocalJusticeAreaType toLocalJusticeAreaType(String ljaType) {
        if (ljaType == null) {
            return null;
        }
        return "PSA".equals(ljaType) ? OpalLocalJusticeAreaType.LJA : OpalLocalJusticeAreaType.valueOf(ljaType);
    }

    default Address courtAddress(OpalLjaRecord dto) {
        if (dto == null || dto.getAddresses() == null || dto.getAddresses().isEmpty()) {
            return null;
        }
        return dto.getAddresses().stream()
            .filter(Objects::nonNull)
            .filter(address -> COURT_ADDRESS_TYPE.equals(address.getAddressType()))
            .findFirst()
            .orElse(null);
    }

    default String courtAddressLine(OpalLjaRecord dto, Function<Address, String> getter) {
        Address address = courtAddress(dto);
        return address == null ? null : getter.apply(address);
    }
}
