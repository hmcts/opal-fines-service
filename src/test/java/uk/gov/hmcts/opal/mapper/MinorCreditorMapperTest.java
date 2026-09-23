package uk.gov.hmcts.opal.mapper;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import uk.gov.hmcts.opal.dto.Creditor;
import uk.gov.hmcts.opal.entity.minorcreditor.MinorCreditorEntity;
import uk.gov.hmcts.opal.generated.model.MinorCreditorAccountSearchCreditor;
import uk.gov.hmcts.opal.generated.model.MinorCreditorAccountSearchResultMinorCreditor;

class MinorCreditorMapperTest {

    private final MinorCreditorMapper minorCreditorMapper = Mappers.getMapper(MinorCreditorMapper.class);

    @Test
    void shouldReturnNullWhenCreditorIsNull() {
        assertThat(minorCreditorMapper.toCreditor(null)).isNull();
    }

    @Test
    void shouldMapMinorCreditorAccountSearchCreditorToCreditor() {
        MinorCreditorAccountSearchCreditor creditor = MinorCreditorAccountSearchCreditor.builder()
            .addressLine1("123 Fake Street")
            .postcode("AB1A 2CD")
            .organisationName("Acme Supplies Ltd.")
            .exactMatchOrganisationName(false)
            .forenames("Jane")
            .surname("Smith")
            .exactMatchSurname(false)
            .exactMatchForenames(false)
            .organisation(false)
            .build();

        Creditor creditorEntity = minorCreditorMapper.toCreditor(creditor);

        assertThat(creditorEntity).isNotNull();
        assertThat(creditorEntity.getAddressLine1()).isEqualTo("123 Fake Street");
        assertThat(creditorEntity.getPostcode()).isEqualTo("AB1A 2CD");
        assertThat(creditorEntity.getOrganisationName()).isEqualTo("Acme Supplies Ltd.");
        assertThat(creditorEntity.getExactMatchOrganisationName()).isFalse();
        assertThat(creditorEntity.getForenames()).isEqualTo("Jane");
        assertThat(creditorEntity.getSurname()).isEqualTo("Smith");
        assertThat(creditorEntity.getExactMatchSurname()).isFalse();
        assertThat(creditorEntity.getExactMatchForenames()).isFalse();
        assertThat(creditorEntity.getOrganisation()).isFalse();
    }

    @Test
    void shouldReturnNullWhenEntityIsNull() {
        assertThat(minorCreditorMapper.toCreditorAccountDto(null)).isNull();
    }

    @Test
    void shouldMapMinorCreditorEntityToCreditorAccountDto() {
        MinorCreditorEntity entity = MinorCreditorEntity.builder()
            .creditorId(123L)
            .postCode("AB1A 2CD")
            .creditorAccountBalance(250)
            .forenames("John")
            .defendantAccountId(456L)
            .organisation(false)
            .organisationName(null)
            .defendantFornames("Jane")
            .defendantSurname("Smith")
            .build();

        MinorCreditorAccountSearchResultMinorCreditor result = minorCreditorMapper.toCreditorAccountDto(entity);

        assertThat(result).isNotNull();
        assertThat(result.getCreditorAccountId()).isEqualTo("123");
        assertThat(result.getPostcode()).isEqualTo("AB1A 2CD");
        assertThat(result.getAccountBalance()).isEqualByComparingTo(new BigDecimal("250"));
        assertThat(result.getFirstnames()).isEqualTo("John");
        assertThat(result.getDefendant().getDefendantAccountId()).isEqualTo("456");
        assertThat(result.getDefendant().getOrganisation()).isFalse();
        assertThat(result.getDefendant().getOrganisationName()).isNull();
        assertThat(result.getDefendant().getFirstnames()).isEqualTo("Jane");
        assertThat(result.getDefendant().getSurname()).isEqualTo("Smith");
    }
}