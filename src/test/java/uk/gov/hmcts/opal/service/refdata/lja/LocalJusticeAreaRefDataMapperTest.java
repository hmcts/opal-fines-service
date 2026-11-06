package uk.gov.hmcts.opal.service.refdata.lja;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import uk.gov.hmcts.opal.entity.LocalJusticeAreaEntity;
import uk.gov.hmcts.opal.entity.LocalJusticeAreaType;
import uk.gov.hmcts.opal.service.refdata.common.Address;

class LocalJusticeAreaRefDataMapperTest {

    private final LocalJusticeAreaRefDataMapper mapper = Mappers.getMapper(LocalJusticeAreaRefDataMapper.class);

    @Test
    void updateEntityFromDto_mapsLjaFieldsAndCourtAddress() {
        // Given
        LjaRecord record = LjaRecord.builder()
            .ljaCode("1234")
            .ljaName("Test LJA")
            .ljaType("CRWCRT")
            .addresses(List.of(
                Address.builder().addressType("Financial Office Address").addressLine1("Financial line").build(),
                Address.builder()
                    .addressType(LjaRecord.COURT_ADDRESS_TYPE)
                    .addressLine1("Court line 1")
                    .addressLine2("Court line 2")
                    .addressLine3("Court line 3")
                    .addressLine4("Court line 4")
                    .addressLine5("Court line 5")
                    .postCode("GIR 0AA")
                    .build()))
            .build();
        LocalJusticeAreaEntity entity = new LocalJusticeAreaEntity();
        entity.setLocalJusticeAreaId((short) 9999);
        entity.setEndDate(LocalDateTime.of(2026, 1, 1, 0, 0));

        // When
        mapper.updateEntityFromDto(record, entity);

        // Then
        assertThat(entity.getLocalJusticeAreaId()).isEqualTo((short) 9999);
        assertThat(entity.getName()).isEqualTo("Test LJA");
        assertThat(entity.getLjaType()).isEqualTo(LocalJusticeAreaType.CRWCRT);
        assertThat(entity.getAddressLine1()).isEqualTo("Court line 1");
        assertThat(entity.getAddressLine2()).isEqualTo("Court line 2");
        assertThat(entity.getAddressLine3()).isEqualTo("Court line 3");
        assertThat(entity.getAddressLine4()).isEqualTo("Court line 4");
        assertThat(entity.getAddressLine5()).isEqualTo("Court line 5");
        assertThat(entity.getPostcode()).isEqualTo("GIR 0AA");
        assertThat(entity.getEndDate()).isEqualTo(LocalDateTime.of(2026, 1, 1, 0, 0));
    }

    @Test
    void toLocalJusticeAreaType_mapsPsaToLja() {
        // Given
        String ljaType = "PSA";

        // When
        LocalJusticeAreaType result = mapper.toLocalJusticeAreaType(ljaType);

        // Then
        assertThat(result).isEqualTo(LocalJusticeAreaType.LJA);
    }

    @Test
    void toLocalJusticeAreaType_rejectsUnknownType() {
        // Given
        String ljaType = "UNKNOWN";

        // When / Then
        assertThatThrownBy(() -> mapper.toLocalJusticeAreaType(ljaType))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void courtAddress_returnsNullWhenNoCourtAddressExists() {
        // Given
        LjaRecord record = LjaRecord.builder()
            .addresses(List.of(Address.builder().addressType("Financial Office Address").build()))
            .build();

        // When
        Address result = mapper.courtAddress(record);

        // Then
        assertThat(result).isNull();
    }
}
