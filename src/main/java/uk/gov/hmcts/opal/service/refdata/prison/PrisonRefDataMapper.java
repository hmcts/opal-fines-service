package uk.gov.hmcts.opal.service.refdata.prison;

import java.util.Objects;
import java.util.function.Function;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.opal.entity.PrisonEntity;
import uk.gov.hmcts.opal.service.refdata.common.Address;

@Component
public class PrisonRefDataMapper {

    private static final String PRISON_ADDRESS_TYPE = "PRIS";

    public void updateEntityFromDto(PrisonRecord dto, PrisonEntity entity) {
        entity.setName(dto.getName());
        entity.setAddressLine1(addressLine(dto, Address::getAddressLine1));
        entity.setAddressLine2(addressLine(dto, Address::getAddressLine2));
        entity.setAddressLine3(addressLine(dto, Address::getAddressLine3));
        entity.setAddressLine4(addressLine(dto, Address::getAddressLine4));
        entity.setAddressLine5(addressLine(dto, Address::getAddressLine5));
        entity.setPostcode(addressLine(dto, Address::getPostCode));
    }

    private String addressLine(PrisonRecord dto, Function<Address, String> getter) {
        if (dto == null || dto.getAddresses() == null) {
            return null;
        }
        return dto.getAddresses().stream()
            .filter(Objects::nonNull)
            .filter(address -> PRISON_ADDRESS_TYPE.equals(address.getAddressType()))
            .findFirst()
            .map(getter)
            .orElse(null);
    }
}
