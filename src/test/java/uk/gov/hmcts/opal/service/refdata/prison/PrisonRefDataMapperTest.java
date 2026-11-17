package uk.gov.hmcts.opal.service.refdata.prison;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.opal.entity.PrisonEntity;
import uk.gov.hmcts.opal.service.refdata.common.Address;

class PrisonRefDataMapperTest {

    private final PrisonRefDataMapper mapper = new PrisonRefDataMapper();

    @Test
    void shouldMapTheFirstPrisAddress() {
        // Given
        PrisonRecord record = recordWithAddresses(
            Address.builder().addressType("OTHER").addressLine1("Ignored").build(),
            Address.builder().addressType("PRIS")
                .addressLine1("Line 1")
                .addressLine2("Line 2")
                .addressLine3("Line 3")
                .addressLine4("Line 4")
                .addressLine5("Line 5")
                .postCode("IP1 1AA")
                .build());
        PrisonEntity entity = new PrisonEntity();

        // When
        mapper.updateEntityFromDto(record, entity);

        // Then
        assertThat(entity.getName()).isEqualTo("Integration Prison");
        assertThat(entity.getAddressLine1()).isEqualTo("Line 1");
        assertThat(entity.getAddressLine2()).isEqualTo("Line 2");
        assertThat(entity.getAddressLine3()).isEqualTo("Line 3");
        assertThat(entity.getAddressLine4()).isEqualTo("Line 4");
        assertThat(entity.getAddressLine5()).isEqualTo("Line 5");
        assertThat(entity.getPostcode()).isEqualTo("IP1 1AA");
    }

    @Test
    void shouldClearAddressWhenThereIsNoPrisAddress() {
        // Given
        PrisonRecord record = recordWithAddresses(
            Address.builder().addressType(null).addressLine1("Not selected").build());
        PrisonEntity entity = PrisonEntity.builder().addressLine1("Old line").postcode("OLD 1AA").build();

        // When
        mapper.updateEntityFromDto(record, entity);

        // Then
        assertThat(entity.getAddressLine1()).isNull();
        assertThat(entity.getPostcode()).isNull();
    }

    private PrisonRecord recordWithAddresses(Address... addresses) {
        return PrisonRecord.builder().name("Integration Prison").addresses(List.of(addresses)).build();
    }
}
