package uk.gov.hmcts.opal.service.refdata.lja;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import uk.gov.hmcts.opal.entity.OpalLocalJusticeAreaEntity;
import uk.gov.hmcts.opal.entity.OpalLocalJusticeAreaType;
import uk.gov.hmcts.opal.service.refdata.common.Address;

class OpalLocalJusticeAreaRefDataMapperTest {

    private final OpalLocalJusticeAreaRefDataMapper mapper = Mappers.getMapper(OpalLocalJusticeAreaRefDataMapper.class);

    @Test
    void updateEntityFromDto_mapsLjaFieldsAndCourtAddress() {
        // Given
        OpalLjaRecord record = OpalLjaRecord.builder()
            .ljaCode("1234")
            .ljaName("Test LJA")
            .ljaType("CRWCRT")
            .addresses(List.of(
                Address.builder().addressType("Financial Office Address").addressLine1("Financial line").build(),
                Address.builder()
                    .addressType(OpalLjaRecord.COURT_ADDRESS_TYPE)
                    .addressLine1("Court line 1")
                    .addressLine2("Court line 2")
                    .addressLine3("Court line 3")
                    .addressLine4("Court line 4")
                    .addressLine5("Court line 5")
                    .postCode("GIR 0AA")
                    .build()))
            .build();
        OpalLocalJusticeAreaEntity entity = new OpalLocalJusticeAreaEntity();
        entity.setLocalJusticeAreaId((short) 9999);
        entity.setEndDate(LocalDateTime.of(2026, 1, 1, 0, 0));

        // When
        mapper.updateEntityFromDto(record, entity);

        // Then
        assertThat(entity.getLocalJusticeAreaId()).isEqualTo((short) 9999);
        assertThat(entity.getName()).isEqualTo("Test LJA");
        assertThat(entity.getLjaType()).isEqualTo(OpalLocalJusticeAreaType.CRWCRT);
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
        OpalLocalJusticeAreaType result = mapper.toLocalJusticeAreaType(ljaType);

        // Then
        assertThat(result).isEqualTo(OpalLocalJusticeAreaType.LJA);
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
        OpalLjaRecord record = OpalLjaRecord.builder()
            .addresses(List.of(Address.builder().addressType("Financial Office Address").build()))
            .build();

        // When
        Address result = mapper.courtAddress(record);

        // Then
        assertThat(result).isNull();
    }
}
