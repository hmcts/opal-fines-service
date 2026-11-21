package uk.gov.hmcts.opal.service.refdata.prosecutor;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import uk.gov.hmcts.opal.entity.OpalProsecutorEntity;
import uk.gov.hmcts.opal.service.refdata.common.Address;

class OpalProsecutorRefDataMapperTest {

    private final OpalProsecutorRefDataMapper mapper = Mappers.getMapper(OpalProsecutorRefDataMapper.class);

    @Test
    void updateEntityFromDto_mapsNameAndProsecutorAddress() {
        // Given
        OpalProsecutorRecord record = OpalProsecutorRecord.builder()
            .prosecutorCode(1234)
            .name("Test Prosecutor")
            .addresses(List.of(
                Address.builder().addressType("Court Address").addressLine1("Court line").build(),
                Address.builder()
                    .addressType(OpalProsecutorRecord.PROSECUTOR_ADDRESS_TYPE)
                    .addressLine1("Prosecutor line 1")
                    .addressLine2("Prosecutor line 2")
                    .addressLine3("Prosecutor line 3")
                    .addressLine4("Prosecutor line 4")
                    .addressLine5("Prosecutor line 5")
                    .postCode("GIR 0AA")
                    .build()))
            .build();
        LocalDateTime endDate = LocalDateTime.of(2026, 1, 1, 0, 0);
        OpalProsecutorEntity entity = new OpalProsecutorEntity();
        entity.setProsecutorCode("9999");
        entity.setEndDate(endDate);

        // When
        mapper.updateEntityFromDto(record, entity);

        // Then
        assertThat(entity.getProsecutorCode()).isEqualTo("9999");
        assertThat(entity.getName()).isEqualTo("Test Prosecutor");
        assertThat(entity.getAddressLine1()).isEqualTo("Prosecutor line 1");
        assertThat(entity.getAddressLine2()).isEqualTo("Prosecutor line 2");
        assertThat(entity.getAddressLine3()).isEqualTo("Prosecutor line 3");
        assertThat(entity.getAddressLine4()).isEqualTo("Prosecutor line 4");
        assertThat(entity.getAddressLine5()).isEqualTo("Prosecutor line 5");
        assertThat(entity.getPostcode()).isEqualTo("GIR 0AA");
        assertThat(entity.getEndDate()).isEqualTo(endDate);
    }

    @Test
    void updateEntityFromDto_clearsAddressFieldsWhenAddressesAreMissing() {
        // Given
        OpalProsecutorRecord record = OpalProsecutorRecord.builder()
            .name("Test Prosecutor")
            .build();
        OpalProsecutorEntity entity = OpalProsecutorEntity.builder()
            .addressLine1("Existing line")
            .postcode("GIR 0AA")
            .build();

        // When
        mapper.updateEntityFromDto(record, entity);

        // Then
        assertThat(entity.getAddressLine1()).isNull();
        assertThat(entity.getPostcode()).isNull();
    }

    @Test
    void prosecutorAddress_returnsNullWhenNoProsecutorAddressExists() {
        // Given
        OpalProsecutorRecord record = OpalProsecutorRecord.builder()
            .addresses(List.of(Address.builder().addressType("Court Address").build()))
            .build();

        // When
        Address result = mapper.prosecutorAddress(record);

        // Then
        assertThat(result).isNull();
    }
}
