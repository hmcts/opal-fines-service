package uk.gov.hmcts.opal.service.refdata.prison;

import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.opal.entity.PrisonEntity;
import uk.gov.hmcts.opal.service.refdata.common.Address;

@Component
public class PrisonRefDataMapper {

    private static final String PRISON_ADDRESS_TYPE = "PRIS";

    public void updateEntityFromDto(PrisonRecord dto, PrisonEntity entity) {
        Address address = prisonAddress(dto).orElse(null);
        entity.setName(dto.getName());
        entity.setAddressLine1(address == null ? null : address.getAddressLine1());
        entity.setAddressLine2(address == null ? null : address.getAddressLine2());
        entity.setAddressLine3(address == null ? null : address.getAddressLine3());
        entity.setAddressLine4(address == null ? null : address.getAddressLine4());
        entity.setAddressLine5(address == null ? null : address.getAddressLine5());
        entity.setPostcode(address == null ? null : address.getPostCode());
    }

    private Optional<Address> prisonAddress(PrisonRecord dto) {
        if (dto == null || dto.getAddresses() == null) {
            return Optional.empty();
        }
        return dto.getAddresses().stream()
            .filter(Objects::nonNull)
            .filter(address -> PRISON_ADDRESS_TYPE.equals(address.getAddressType()))
            .findFirst();
    }
}
