package uk.gov.hmcts.opal.mapper.legacy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigInteger;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import uk.gov.hmcts.opal.dto.GetDefendantAccountConsolidatedAccountsResult;
import uk.gov.hmcts.opal.dto.legacy.LegacyConsolidatedAccount;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetDefendantAccountConsolidatedAccountsResponse;
import uk.gov.hmcts.opal.generated.model.ConsolidatedAccountDefendantAccount;

class LegacyConsolidatedAccountMapperTest {

    private final LegacyConsolidatedAccountMapper mapper = Mappers.getMapper(LegacyConsolidatedAccountMapper.class);

    @Test
    void toResponse_mapsVersionAndSortedPayload() {
        LegacyGetDefendantAccountConsolidatedAccountsResponse legacy =
            LegacyGetDefendantAccountConsolidatedAccountsResponse.builder()
                .version(7L)
                .consolidatedAccounts(List.of(
                    legacyAccount(233302L, "233302C"),
                    legacyAccount(233301L, "233301C")
                ))
                .build();

        GetDefendantAccountConsolidatedAccountsResult response = mapper.toResponse(legacy);

        assertEquals(BigInteger.valueOf(7), response.getVersion());
        assertEquals(2, response.getPayload().size());

        ConsolidatedAccountDefendantAccount first = response.getPayload().getFirst();
        assertEquals(233301L, first.getAccountId());
        assertEquals("233301C", first.getAccountNumber());
        assertEquals("Alex", first.getFirstName());
        assertEquals("Jones", first.getLastName());
        assertEquals(LocalDate.parse("2026-01-21"), first.getDateImposed());
        assertEquals("Child Court", first.getImposedBy());
        assertEquals("CHILD-REF", first.getReference());
    }

    @Test
    void toResponse_whenLegacyAccountsNull_returnsEmptyPayload() {
        LegacyGetDefendantAccountConsolidatedAccountsResponse legacy =
            LegacyGetDefendantAccountConsolidatedAccountsResponse.builder()
                .version(1L)
                .consolidatedAccounts(null)
                .build();

        GetDefendantAccountConsolidatedAccountsResult response = mapper.toResponse(legacy);

        assertEquals(BigInteger.ONE, response.getVersion());
        assertEquals(List.of(), response.getPayload());
    }

    private LegacyConsolidatedAccount legacyAccount(Long accountId, String accountNumber) {
        return LegacyConsolidatedAccount.builder()
            .accountId(accountId)
            .accountNumber(accountNumber)
            .firstName("Alex")
            .lastName("Jones")
            .dateImposed(LocalDate.parse("2026-01-21"))
            .imposedBy("Child Court")
            .reference("CHILD-REF")
            .build();
    }
}
