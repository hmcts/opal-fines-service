package uk.gov.hmcts.opal.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.openapitools.jackson.nullable.JsonNullable;
import uk.gov.hmcts.opal.dto.DefendantAccountSummaryDto;
import uk.gov.hmcts.opal.dto.DefendantAccountSummaryDto.Checks;
import uk.gov.hmcts.opal.dto.DefendantAccountSummaryDto.WarnError;
import uk.gov.hmcts.opal.dto.search.AccountSearchDto;
import uk.gov.hmcts.opal.dto.search.AliasDto;
import uk.gov.hmcts.opal.dto.search.DefendantAccountSearchResultsDto;
import uk.gov.hmcts.opal.generated.model.DefendantAccountSearchAliasDefendantAccount;
import uk.gov.hmcts.opal.generated.model.DefendantAccountSearchCheckDefendantAccount;
import uk.gov.hmcts.opal.generated.model.DefendantAccountSearchChecksDefendantAccount;
import uk.gov.hmcts.opal.generated.model.DefendantAccountSearchDefendantDefendantAccount;
import uk.gov.hmcts.opal.generated.model.DefendantAccountSearchReferenceNumberDefendantAccount;
import uk.gov.hmcts.opal.generated.model.DefendantAccountSearchResultDefendantAccount;
import uk.gov.hmcts.opal.generated.model.PostDefendantAccountSearchRequestDefendantAccount;
import uk.gov.hmcts.opal.generated.model.PostDefendantAccountSearchResponseDefendantAccount;

class DefendantAccountSearchMapperTest {

    private final DefendantAccountSearchMapper mapper = Mappers.getMapper(DefendantAccountSearchMapper.class);

    @Test
    void toDto_mapsGeneratedRequestToInternalDto() {
        //Arrange
        DefendantAccountSearchReferenceNumberDefendantAccount referenceNumber =
            DefendantAccountSearchReferenceNumberDefendantAccount.builder()
                .organisation(false)
                .accountNumber("23000000001")
                .prosecutorCaseReference(JsonNullable.of(null))
                .build();
        DefendantAccountSearchDefendantDefendantAccount defendant =
            DefendantAccountSearchDefendantDefendantAccount.builder()
                .includeAliases(true)
                .organisation(false)
                .addressLine1("1 High Street")
                .postcode("AB1 2CD")
                .organisationName(JsonNullable.of(null))
                .exactMatchOrganisationName(false)
                .surname("Smith")
                .exactMatchSurname(true)
                .forenames("Jane")
                .exactMatchForenames(false)
                .birthDate(LocalDate.of(1980, 2, 3))
                .nationalInsuranceNumber("QQ123456C")
                .build();
        PostDefendantAccountSearchRequestDefendantAccount request =
            PostDefendantAccountSearchRequestDefendantAccount.builder()
                .activeAccountsOnly(true)
                .businessUnitIds(List.of(77, 78))
                .consolidationSearch(true)
                .referenceNumber(referenceNumber)
                .defendant(defendant)
                .build();

        //Act
        AccountSearchDto result = mapper.toDto(request);

        //Assert
        assertTrue(result.getActiveAccountsOnly());
        assertEquals(List.of((short) 77, (short) 78), result.getBusinessUnitIds());
        assertTrue(result.getConsolidationSearch());
        assertFalse(result.getReferenceNumberDto().getOrganisation());
        assertEquals("23000000001", result.getReferenceNumberDto().getAccountNumber());
        assertNull(result.getReferenceNumberDto().getProsecutorCaseReference());
        assertTrue(result.getDefendant().getIncludeAliases());
        assertFalse(result.getDefendant().getOrganisation());
        assertEquals("1 High Street", result.getDefendant().getAddressLine1());
        assertEquals("AB1 2CD", result.getDefendant().getPostcode());
        assertNull(result.getDefendant().getOrganisationName());
        assertFalse(result.getDefendant().getExactMatchOrganisationName());
        assertEquals("Smith", result.getDefendant().getSurname());
        assertTrue(result.getDefendant().getExactMatchSurname());
        assertEquals("Jane", result.getDefendant().getForenames());
        assertFalse(result.getDefendant().getExactMatchForenames());
        assertEquals(LocalDate.of(1980, 2, 3), result.getDefendant().getBirthDate());
        assertEquals("QQ123456C", result.getDefendant().getNationalInsuranceNumber());
    }

    @Test
    void toResponse_mapsInternalDtoToGeneratedResponse() {
        //Arrange
        DefendantAccountSummaryDto summary = DefendantAccountSummaryDto.builder()
            .defendantAccountId("1000000001")
            .accountNumber("23000000001")
            .organisation(false)
            .aliases(List.of(AliasDto.builder()
                .aliasNumber(1)
                .organisationName(null)
                .surname("Jones")
                .forenames("Janet")
                .build()))
            .addressLine1("1 High Street")
            .postcode("AB1 2CD")
            .businessUnitName("London Collection")
            .businessUnitId("77")
            .prosecutorCaseReference("PCR123")
            .lastEnforcementAction("Warrant")
            .accountBalance(new BigDecimal("123.45"))
            .organisationName(null)
            .defendantTitle("Ms")
            .defendantFirstnames("Jane")
            .defendantSurname("Smith")
            .birthDate("1980-02-03")
            .nationalInsuranceNumber("QQ123456C")
            .parentGuardianSurname("Guardian")
            .parentGuardianFirstnames("Pat")
            .hasCollectionOrder(true)
            .accountVersion(BigInteger.valueOf(7))
            .checks(Checks.builder()
                .warnings(List.of(new WarnError("W01|Warning message")))
                .errors(List.of(new WarnError("E01|Error message")))
                .build())
            .build();
        DefendantAccountSearchResultsDto dto = DefendantAccountSearchResultsDto.builder()
            .defendantAccounts(List.of(summary))
            .build();

        //Act
        PostDefendantAccountSearchResponseDefendantAccount response = mapper.toResponse(dto);

        //Assert
        assertEquals(1, response.getCount());
        DefendantAccountSearchResultDefendantAccount account = value(response.getDefendantAccounts()).getFirst();
        assertEquals("1000000001", account.getDefendantAccountId());
        assertEquals("23000000001", account.getAccountNumber());
        assertFalse(account.getOrganisation());
        assertEquals("1 High Street", account.getAddressLine1());
        assertEquals("AB1 2CD", value(account.getPostcode()));
        assertEquals("London Collection", account.getBusinessUnitName());
        assertEquals("77", account.getBusinessUnitId());
        assertEquals("PCR123", value(account.getProsecutorCaseReference()));
        assertEquals("Warrant", value(account.getLastEnforcementAction()));
        assertEquals(new BigDecimal("123.45"), account.getAccountBalance());
        assertNull(value(account.getOrganisationName()));
        assertEquals("Ms", value(account.getDefendantTitle()));
        assertEquals("Jane", value(account.getDefendantFirstnames()));
        assertEquals("Smith", value(account.getDefendantSurname()));
        assertEquals(LocalDate.of(1980, 2, 3), value(account.getBirthDate()));
        assertEquals("QQ123456C", value(account.getNationalInsuranceNumber()));
        assertEquals("Guardian", value(account.getParentGuardianSurname()));
        assertEquals("Pat", value(account.getParentGuardianFirstnames()));
        assertTrue(value(account.getHasCollectionOrder()));
        assertEquals(7, value(account.getAccountVersion()));

        DefendantAccountSearchAliasDefendantAccount alias = value(account.getAliases()).getFirst();
        assertEquals(1, alias.getAliasNumber());
        assertNull(value(alias.getOrganisationName()));
        assertEquals("Jones", value(alias.getSurname()));
        assertEquals("Janet", value(alias.getForenames()));

        DefendantAccountSearchChecksDefendantAccount checks = value(account.getChecks());
        DefendantAccountSearchCheckDefendantAccount warning = checks.getWarnings().getFirst();
        assertEquals("W01", warning.getReference());
        assertEquals("Warning message", warning.getMessage());
        DefendantAccountSearchCheckDefendantAccount error = checks.getErrors().getFirst();
        assertEquals("E01", error.getReference());
        assertEquals("Error message", error.getMessage());
    }

    private <T> T value(JsonNullable<T> nullable) {
        return nullable.get();
    }
}
