package uk.gov.hmcts.opal.mapper.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import uk.gov.hmcts.opal.entity.EnforcerEntity;
import uk.gov.hmcts.opal.generated.model.EnforcerDefendantAccount;

class EnforcerDefendantAccountMapperTest {

    private final EnforcerDefendantAccountMapper mapper = Mappers.getMapper(EnforcerDefendantAccountMapper.class);

    @Test
    void toDto_shouldMapEnforcerId() {
        EnforcerEntity entity = EnforcerEntity.builder()
            .enforcerId(22L)
            .build();

        EnforcerDefendantAccount dto = mapper.toDto(entity);

        assertNotNull(dto);
        assertEquals(22L, dto.getEnforcerId());
    }

    @Test
    void toDto_shouldReturnNull_whenEntityIsNull() {
        assertNull(mapper.toDto(null));
    }
}
