package uk.gov.hmcts.opal.mapper.request;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.opal.generated.model.DefendantAccountSearchDefendantDefendantAccount;
import uk.gov.hmcts.opal.generated.model.DefendantAccountSearchReferenceNumberDefendantAccount;
import uk.gov.hmcts.opal.generated.model.PostDefendantAccountSearchRequestDefendantAccount;

class DefendantAccountSearchRequestMapperTest {

    private final DefendantAccountSearchRequestMapper mapper = new DefendantAccountSearchRequestMapperImpl();

    @Test
    void toAccountSearchDto_mapsGeneratedRequestToInternalDto() {
        // Arrange
        PostDefendantAccountSearchRequestDefendantAccount request =
            PostDefendantAccountSearchRequestDefendantAccount.builder()
                .activeAccountsOnly(true)
                .businessUnitIds(List.of(78, 101))
                .consolidationSearch(true)
                .referenceNumber(new DefendantAccountSearchReferenceNumberDefendantAccount()
                    .organisation(false)
                    .accountNumber("AC123")
                    .prosecutorCaseReference(null))
                .defendant(new DefendantAccountSearchDefendantDefendantAccount()
                    .includeAliases(true)
                    .organisation(false)
                    .addressLine1("1 Test Street")
                    .postcode("AB1 2CD")
                    .organisationName(null)
                    .exactMatchOrganisationName(null)
                    .surname("Smith")
                    .exactMatchSurname(true)
                    .forenames("John")
                    .exactMatchForenames(false)
                    .birthDate(LocalDate.of(1985, 6, 15))
                    .nationalInsuranceNumber("QQ123456C"))
                .build();

        // Act
        var result = mapper.toAccountSearchDto(request);

        // Assert
        assertTrue(result.getActiveAccountsOnly());
        assertEquals(List.of((short) 78, (short) 101), result.getBusinessUnitIds());
        assertTrue(result.isConsolidationSearch());

        assertEquals(Boolean.FALSE, result.getReferenceNumberDto().getOrganisation());
        assertEquals("AC123", result.getReferenceNumberDto().getAccountNumber());
        assertNull(result.getReferenceNumberDto().getProsecutorCaseReference());

        assertTrue(result.getDefendant().getIncludeAliases());
        assertFalse(result.getDefendant().getOrganisation());
        assertEquals("1 Test Street", result.getDefendant().getAddressLine1());
        assertEquals("AB1 2CD", result.getDefendant().getPostcode());
        assertEquals("Smith", result.getDefendant().getSurname());
        assertEquals(Boolean.TRUE, result.getDefendant().getExactMatchSurname());
        assertEquals("John", result.getDefendant().getForenames());
        assertEquals(Boolean.FALSE, result.getDefendant().getExactMatchForenames());
        assertEquals(LocalDate.of(1985, 6, 15), result.getDefendant().getBirthDate());
        assertEquals("QQ123456C", result.getDefendant().getNationalInsuranceNumber());
    }

    @Test
    void mapBusinessUnitIds_returnsNullWhenInputNull() {
        // Arrange / Act / Assert
        assertNull(mapper.mapBusinessUnitIds(null));
    }

    @Test
    void toShort_returnsNullWhenInputNull() {
        // Arrange / Act / Assert
        assertNull(mapper.toShort(null));
    }
}
