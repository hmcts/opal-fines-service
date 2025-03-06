package uk.gov.hmcts.opal.dto.legacy;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.opal.dto.ToJsonString;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static uk.gov.hmcts.opal.dto.legacy.DefendantAccountSearchCriteria.DefendantAccountSearchCriteriaBuilder;

@Slf4j(topic = "opal.DefendantAccountSearchCriteriaTest")
public class DefendantAccountSearchCriteriaTest {

    @Test
    public void testBuilder() {
        DefendantAccountSearchCriteria criteria = constructDefendantAccountSearchCriteria();

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
    public void testNullBusinessUnit() {
        DefendantAccountSearchCriteriaBuilder criteriaBuilder = constructDefendantAccountSearchCriteriaBuilder();
        DefendantAccountSearchCriteria criteria = criteriaBuilder.businessUnitId(null).build();
        assertNull(criteria.getBusinessUnitId());
        assertEquals(getJsonRepresentation(), criteria.toPrettyJson());
    }

    @Test
    public void testJsonString() throws Exception {
        DefendantAccountSearchCriteria model = constructDefendantAccountSearchCriteria();
        assertNotNull(model.toJsonString());

        DefendantAccountSearchCriteria parsed = ToJsonString.getObjectMapper()
            .readValue(getJsonRepresentation(), DefendantAccountSearchCriteria.class);
        assertNotNull(parsed);
    }

    private DefendantAccountSearchCriteria constructDefendantAccountSearchCriteria() {
        return constructDefendantAccountSearchCriteriaBuilder().build();
    }

    private DefendantAccountSearchCriteriaBuilder constructDefendantAccountSearchCriteriaBuilder() {
        return DefendantAccountSearchCriteria.builder()
            .accountNumber("accountNo")
            .addressLine1("Glasgow")
            .businessUnitId(1L)
            .firstRowNumber(4)
            .lastRowNumber(44)
            .surname("Smith")
            .forenames("John")
            .initials("D")
            .birthDate("1977-06-26")
            .nationalInsuranceNumber("XX123456C");
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
