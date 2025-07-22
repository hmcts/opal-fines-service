package uk.gov.hmcts.opal.dto.legacy;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.opal.dto.ToJsonString;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@Slf4j(topic = "opal.LegacyDefendantAccountSearchCriteriaTest")
public class LegacyDefendantAccountSearchCriteriaTest {

    @Test
    void testBuilder() {
        LegacyDefendantAccountSearchCriteria criteria = constructDefendantAccountSearchCriteria();

        assertEquals("accountNo", criteria.getAccountNumber());
        assertEquals("John", criteria.getForenames());
        assertEquals("D", criteria.getInitials());
        assertEquals("Smith", criteria.getSurname());
        assertEquals("1977-06-26", criteria.getBirthDate());
        assertEquals("Glasgow", criteria.getAddressLine1());
        assertEquals("XX123456C", criteria.getNationalInsuranceNumber());
        assertEquals(1L, criteria.getBusinessUnitId());
        assertNull(criteria.getOrganisationName());
        assertNull(criteria.getProsecutorCaseReference());
    }

    @Test
    void testNullBusinessUnit() {
        LegacyDefendantAccountSearchCriteria criteria = constructDefendantAccountSearchCriteria();
        criteria.setBusinessUnitId(null);
        assertNull(criteria.getBusinessUnitId());
        assertEquals(getJsonRepresentation(), criteria.toPrettyJson());
    }

    @Test
    void testJsonString() throws Exception {
        LegacyDefendantAccountSearchCriteria model = constructDefendantAccountSearchCriteria();
        assertNotNull(model.toJsonString());

        LegacyDefendantAccountSearchCriteria parsed = ToJsonString.getObjectMapper()
            .readValue(getJsonRepresentation(), LegacyDefendantAccountSearchCriteria.class);
        assertNotNull(parsed);
    }

    private LegacyDefendantAccountSearchCriteria constructDefendantAccountSearchCriteria() {
        return LegacyDefendantAccountSearchCriteria.builder()
            .accountNumber("accountNo")
            .addressLine1("Glasgow")
            .businessUnitId(1L)
            .firstRowNumber(4)
            .lastRowNumber(44)
            .surname("Smith")
            .forenames("John")
            .initials("D")
            .birthDate("1977-06-26")
            .nationalInsuranceNumber("XX123456C")
            .build();
    }

    private String getJsonRepresentation() {
        return """
            {
              "surname" : "Smith",
              "forenames" : "John",
              "initials" : "D",
              "firstRowNumber" : 4,
              "lastRowNumber" : 44,
              "account_number" : "accountNo",
              "birth_date" : "1977-06-26",
              "national_insurance_number" : "XX123456C",
              "address_line_1" : "Glasgow"
            }""";
    }
}
