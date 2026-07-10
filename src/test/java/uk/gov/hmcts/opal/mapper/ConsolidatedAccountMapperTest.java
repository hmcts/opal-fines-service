package uk.gov.hmcts.opal.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import uk.gov.hmcts.opal.entity.defendantaccount.ConsolidatedAccountEntity;
import uk.gov.hmcts.opal.generated.model.ConsolidatedAccountDefendantAccount;

class ConsolidatedAccountMapperTest {

    private final ConsolidatedAccountMapper mapper = Mappers.getMapper(ConsolidatedAccountMapper.class);

    @Test
    void toResponse_mapsViewFieldsToResponseFields() {
        LocalDate dateImposed = LocalDate.of(2026, 1, 21);
        ConsolidatedAccountEntity entity = ConsolidatedAccountEntity.builder()
            .childAccountId(123L)
            .childAccountNumber("ACC123")
            .childFirstName("Alex")
            .childLastName("Jones")
            .childDateImposed(dateImposed)
            .childImposedBy("Court A")
            .childReference("REF123")
            .build();

        ConsolidatedAccountDefendantAccount result = mapper.toResponse(entity);

        assertEquals(123L, result.getAccountId());
        assertEquals("ACC123", result.getAccountNumber());
        assertEquals("Alex", result.getFirstName());
        assertEquals("Jones", result.getLastName());
        assertEquals(dateImposed, result.getDateImposed());
        assertEquals("Court A", result.getImposedBy());
        assertEquals("REF123", result.getReference());
    }
}
