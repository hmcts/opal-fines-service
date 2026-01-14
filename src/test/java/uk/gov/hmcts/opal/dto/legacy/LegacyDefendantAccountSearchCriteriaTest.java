package uk.gov.hmcts.opal.dto.legacy;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.opal.dto.search.AccountSearchDto;
import uk.gov.hmcts.opal.dto.search.DefendantDto;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j(topic = "opal.LegacyDefendantAccountSearchCriteriaTest")
class LegacyDefendantAccountSearchCriteriaTest {

    @Test
    void testBuilder() {
        LegacyDefendantAccountSearchCriteria criteria = constructCriteria();

        assertNotNull(criteria.getBusinessUnitIds());
        assertEquals(List.of((short)10), criteria.getBusinessUnitIds());

        assertTrue(criteria.getActiveAccountsOnly());

        assertNotNull(criteria.getDefendant());
        assertNull(criteria.getReferenceNumberDto()); // mutually exclusive test
    }

    @Test
    void testNullReferenceNumberAllowsDefendant() {
        LegacyDefendantAccountSearchCriteria criteria = constructCriteria();
        criteria.setReferenceNumberDto(null);
        assertNotNull(criteria.getDefendant());
    }

    @Test
    void testFromAccountSearchDto() {
        AccountSearchDto dto = AccountSearchDto.builder()
            .businessUnitIds(List.of((short)10))
            .activeAccountsOnly(true)
            .defendant(new DefendantDto())
            .build();

        LegacyDefendantAccountSearchCriteria criteria = LegacyDefendantAccountSearchCriteria.fromAccountSearchDto(dto);

        assertEquals(dto.getBusinessUnitIds(), criteria.getBusinessUnitIds());
        assertEquals(dto.getActiveAccountsOnly(), criteria.getActiveAccountsOnly());
        assertEquals(dto.getDefendant(), criteria.getDefendant());
    }

    @Test
    void testJsonStringSerialization() throws Exception {
        LegacyDefendantAccountSearchCriteria criteria = constructCriteria();

        String json = criteria.toJsonString();
        assertNotNull(json);
        assertTrue(json.contains("\"business_unit_ids\""));
        assertTrue(json.contains("\"active_accounts_only\""));
        assertTrue(json.contains("\"defendant\""));
    }

    private LegacyDefendantAccountSearchCriteria constructCriteria() {
        return LegacyDefendantAccountSearchCriteria.builder()
            .businessUnitIds(List.of((short)10))
            .activeAccountsOnly(true)
            .defendant(new DefendantDto())
            .build();
    }
}
