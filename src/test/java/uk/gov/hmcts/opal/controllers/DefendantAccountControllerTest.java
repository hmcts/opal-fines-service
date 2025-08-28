package uk.gov.hmcts.opal.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.dto.DefendantAccountHeaderSummary;
import uk.gov.hmcts.opal.dto.search.AccountSearchDto;
import uk.gov.hmcts.opal.dto.search.DefendantAccountSearchResultsDto;
import uk.gov.hmcts.opal.service.DefendantAccountService;
import uk.gov.hmcts.opal.service.opal.UserStateService;
import uk.gov.hmcts.opal.service.proxy.DefendantAccountServiceProxy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;

import uk.gov.hmcts.opal.dto.response.GetHeaderSummaryResponse;
import uk.gov.hmcts.opal.dto.common.AccountStatusReference;
import uk.gov.hmcts.opal.dto.common.BusinessUnitSummary;
import uk.gov.hmcts.opal.dto.common.IndividualDetails;
import uk.gov.hmcts.opal.dto.common.OrganisationDetails;
import uk.gov.hmcts.opal.dto.common.PartyDetails;
import uk.gov.hmcts.opal.dto.common.PaymentStateSummary;
import uk.gov.hmcts.opal.service.opal.OpalDefendantAccountService;
import uk.gov.hmcts.opal.service.legacy.LegacyDefendantAccountService;
import uk.gov.hmcts.opal.entity.DefendantAccountHeaderViewEntity;


@ExtendWith(MockitoExtension.class)
class DefendantAccountControllerTest {

    static final String BEARER_TOKEN = "Bearer a_token_here";

    @Mock
    private DefendantAccountService defendantAccountService;

    @Mock
    private UserStateService userStateService;

    @Mock
    private DefendantAccountServiceProxy defendantAccountServiceProxy;

    @InjectMocks
    private DefendantAccountController defendantAccountController;

    @Test
    void testPostDefendantAccountSearch_Success() {
        // Arrange
        AccountSearchDto requestEntity = AccountSearchDto.builder().build();
        DefendantAccountSearchResultsDto mockResponse = DefendantAccountSearchResultsDto.builder().build();

        when(defendantAccountService.searchDefendantAccounts(any(AccountSearchDto.class), eq(BEARER_TOKEN)))
            .thenReturn(mockResponse);

        // Act
        ResponseEntity<DefendantAccountSearchResultsDto> responseEntity =
            defendantAccountController.postDefendantAccountSearch(requestEntity, BEARER_TOKEN);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(mockResponse, responseEntity.getBody());

        verify(defendantAccountService, times(1))
            .searchDefendantAccounts(any(AccountSearchDto.class), eq(BEARER_TOKEN));
    }

    @Test
    void testGetHeaderSummary_Success() {
        // Arrange
        DefendantAccountHeaderSummary mockBody = new DefendantAccountHeaderSummary();

        var userWithPermission = uk.gov.hmcts.opal.authorisation.model.UserState.builder()
            .userId(99L)
            .userName("tester")
            .businessUnitUser(java.util.Set.of(
                uk.gov.hmcts.opal.authorisation.model.BusinessUnitUser.builder()
                    .businessUnitUserId("1L")
                    .businessUnitId((short) 78)
                    .build()
            ))
            .build();

        when(defendantAccountService.getHeaderSummary(eq(1L), any()))
            .thenReturn(mockBody);

        // Act
        ResponseEntity<DefendantAccountHeaderSummary> response =
            defendantAccountController.getHeaderSummary(1L, BEARER_TOKEN);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockBody, response.getBody());

        verify(defendantAccountService).getHeaderSummary(eq(1L), any());
    }


    @Test
    void testGetHeaderSummaryResponseGetters() {
        DefendantAccountHeaderSummary summary = DefendantAccountHeaderSummary.builder()
            .accountNumber("ABCD")
            .accountType("Fine")
            .build();
        Long version = 10L;

        GetHeaderSummaryResponse resp = new GetHeaderSummaryResponse(summary, version);
        assertEquals(summary, resp.getData());
        assertEquals(version, resp.getVersion());

        // Null cases
        GetHeaderSummaryResponse respNull = new GetHeaderSummaryResponse(null, null);
        assertNull(respNull.getData());
        assertNull(respNull.getVersion());
    }

    @Test
    void testOpalDefendantAccountService_mapToDto_andSupportMethods() {
        // Mock entity with all fields
        DefendantAccountHeaderViewEntity entity = DefendantAccountHeaderViewEntity.builder()
            .partyId(123L)
            .parentGuardianAccountPartyId(456L)
            .accountNumber("X123")
            .accountType("Fine")
            .prosecutorCaseReference("PCR")
            .fixedPenaltyTicketNumber("FPT123")
            .accountStatus("L")
            .businessUnitId((short) 8)
            .businessUnitName("Some BU")
            .imposed(BigDecimal.valueOf(10))
            .arrears(BigDecimal.valueOf(2))
            .paid(BigDecimal.valueOf(3))
            .accountBalance(BigDecimal.valueOf(5))
            .organisation(false)
            .organisationName("Test Org")
            .title("Mr")
            .firstnames("Joe")
            .surname("Bloggs")
            .birthDate(LocalDate.of(1990, 1, 2))
            .build();

        OpalDefendantAccountService service = new OpalDefendantAccountService(null, null, null);

        // Use reflection to call mapToDto (since it's private)
        try {
            Method m = OpalDefendantAccountService.class.getDeclaredMethod("mapToDto", DefendantAccountHeaderViewEntity.class);
            m.setAccessible(true);
            DefendantAccountHeaderSummary dto = (DefendantAccountHeaderSummary) m.invoke(service, entity);
            assertNotNull(dto);
            assertEquals("X123", dto.getAccountNumber());
            assertNotNull(dto.getPartyDetails());
            assertEquals("123", dto.getPartyDetails().getPartyId());
            assertEquals("Mr", dto.getPartyDetails().getIndividualDetails().getTitle());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testOpalDefendantAccountService_nz_and_calculateAge() throws Exception {
        OpalDefendantAccountService service = new OpalDefendantAccountService(null, null, null);

        Method nzMethod = OpalDefendantAccountService.class.getDeclaredMethod("nz", BigDecimal.class);
        nzMethod.setAccessible(true);

        assertEquals(BigDecimal.valueOf(100), nzMethod.invoke(null, BigDecimal.valueOf(100)));
        assertEquals(BigDecimal.ZERO, nzMethod.invoke(null, (Object) null));

        Method ageMethod = OpalDefendantAccountService.class.getDeclaredMethod("calculateAge", LocalDate.class);
        ageMethod.setAccessible(true);

        int thisYear = LocalDate.now().getYear();
        int age = (int) ageMethod.invoke(service, LocalDate.of(thisYear - 40, 1, 1));
        assertEquals(40, age);
        int zeroAge = (int) ageMethod.invoke(service, (Object) null);
        assertEquals(0, zeroAge);
    }

    @Test
    void testOpalDefendantAccountService_resolveStatusDisplayName() throws Exception {
        OpalDefendantAccountService service = new OpalDefendantAccountService(null, null, null);
        Method m = OpalDefendantAccountService.class.getDeclaredMethod("resolveStatusDisplayName", String.class);
        m.setAccessible(true);
        assertEquals("Live", m.invoke(service, "L"));
        assertEquals("Completed", m.invoke(service, "C"));
        assertEquals("TFO to be acknowledged", m.invoke(service, "TO"));
        assertEquals("TFO to NI/Scotland to be acknowledged", m.invoke(service, "TS"));
        assertEquals("TFO acknowledged", m.invoke(service, "TA"));
        assertEquals("Account consolidated", m.invoke(service, "CS"));
        assertEquals("Account written off", m.invoke(service, "WO"));
        assertEquals("Unknown", m.invoke(service, "something-else"));
    }

    @Test
    void testLegacyDefendantAccountService_toBigDecimalOrZero() throws Exception {
        Method m = LegacyDefendantAccountService.class.getDeclaredMethod("toBigDecimalOrZero", String.class);
        m.setAccessible(true);
        assertEquals(BigDecimal.ZERO, m.invoke(null, (String) null));
        assertEquals(BigDecimal.ZERO, m.invoke(null, "not_a_number"));
        assertEquals(new BigDecimal("12.34"), m.invoke(null, "12.34"));
    }

    @Test
    void testLegacyDefendantAccountService_createGetDefendantAccountRequest() {
        String id = "77";
        var req = LegacyDefendantAccountService.createGetDefendantAccountRequest(id);
        assertNotNull(req);
        assertEquals(id, req.getDefendantAccountId());
    }

    @Test
    void testPartyDetailsBuilderAllNulls() {
        PartyDetails pd = PartyDetails.builder().build();
        assertNull(pd.getPartyId());
        assertNull(pd.getOrganisationFlag());
        assertNull(pd.getOrganisationDetails());
        assertNull(pd.getIndividualDetails());
    }

    @Test
    void testBusinessUnitSummaryBuilder() {
        BusinessUnitSummary bu = BusinessUnitSummary.builder()
            .businessUnitId("BU77")
            .businessUnitName("North East")
            .welshSpeaking("N")
            .build();
        assertEquals("BU77", bu.getBusinessUnitId());
        assertEquals("North East", bu.getBusinessUnitName());
        assertEquals("N", bu.getWelshSpeaking());
    }

    @Test
    void testAccountStatusReferenceBuilder() {
        AccountStatusReference ref = AccountStatusReference.builder()
            .accountStatusCode("L")
            .accountStatusDisplayName("Live")
            .build();
        assertEquals("L", ref.getAccountStatusCode());
        assertEquals("Live", ref.getAccountStatusDisplayName());
    }

    @Test
    void testIndividualDetailsBuilder() {
        IndividualDetails details = IndividualDetails.builder()
            .title("Mr")
            .forenames("John")
            .surname("Smith")
            .dateOfBirth("2000-01-01")
            .age("24")
            .nationalInsuranceNumber("XX999999X")
            .individualAliases(Collections.emptyList())
            .build();
        assertEquals("Mr", details.getTitle());
        assertEquals("John", details.getForenames());
        assertEquals("Smith", details.getSurname());
        assertEquals("2000-01-01", details.getDateOfBirth());
        assertEquals("24", details.getAge());
        assertEquals("XX999999X", details.getNationalInsuranceNumber());
        assertTrue(details.getIndividualAliases().isEmpty());
    }

    @Test
    void testOrganisationDetailsBuilder() {
        OrganisationDetails org = OrganisationDetails.builder()
            .organisationName("Big Org")
            .organisationAliases(Collections.emptyList())
            .build();
        assertEquals("Big Org", org.getOrganisationName());
        assertTrue(org.getOrganisationAliases().isEmpty());
    }

    @Test
    void testPaymentStateSummaryBuilder() {
        PaymentStateSummary pay = PaymentStateSummary.builder()
            .imposedAmount(BigDecimal.TEN)
            .arrearsAmount(BigDecimal.ONE)
            .paidAmount(BigDecimal.ZERO)
            .accountBalance(BigDecimal.TEN)
            .build();
        assertEquals(BigDecimal.TEN, pay.getImposedAmount());
        assertEquals(BigDecimal.ONE, pay.getArrearsAmount());
        assertEquals(BigDecimal.ZERO, pay.getPaidAmount());
        assertEquals(BigDecimal.TEN, pay.getAccountBalance());
    }

    @Test
    void testOpalDefendantAccountService_mapToDto_nullsSafe() throws Exception {
        OpalDefendantAccountService service = new OpalDefendantAccountService(null, null, null);
        DefendantAccountHeaderViewEntity e = new DefendantAccountHeaderViewEntity();
        e.setAccountStatus("L"); // <-- set a default status
        Method m = OpalDefendantAccountService.class.getDeclaredMethod("mapToDto", DefendantAccountHeaderViewEntity.class);
        m.setAccessible(true);
        var dto = (DefendantAccountHeaderSummary) m.invoke(service, e);
        assertNotNull(dto);
    }


    @Test
    void testLegacyDefendantAccountService_toHeaderSumaryDto_fullAndNullCases() {
        var legacy = new uk.gov.hmcts.opal.dto.legacy.LegacyGetDefendantAccountHeaderSummaryResponse();
        var party = uk.gov.hmcts.opal.dto.legacy.common.LegacyPartyDetails.builder()
            .defendantAccountPartyId("55")
            .organisationFlag(true)
            .organisationDetails(
                uk.gov.hmcts.opal.dto.legacy.common.OrganisationDetails.builder()
                    .organisationName("Acme Org")
                    .organisationAliases(new uk.gov.hmcts.opal.dto.legacy.common.OrganisationDetails.OrganisationAlias[]{
                        uk.gov.hmcts.opal.dto.legacy.common.OrganisationDetails.OrganisationAlias.builder()
                            .aliasId("1")
                            .sequenceNumber((short) 1)
                            .organisationName("AliasOrg")
                            .build()
                    })
                    .build()
            )
            .individualDetails(
                uk.gov.hmcts.opal.dto.legacy.common.IndividualDetails.builder()
                    .title("Mx")
                    .firstNames("Jamie")
                    .surname("Bloggs")
                    .dateOfBirth(java.time.LocalDate.of(1985, 5, 1))
                    .age("39")
                    .nationalInsuranceNumber("ZZ999999Z")
                    .individualAliases(new uk.gov.hmcts.opal.dto.legacy.common.IndividualDetails.IndividualAlias[]{
                        uk.gov.hmcts.opal.dto.legacy.common.IndividualDetails.IndividualAlias.builder()
                            .aliasId("a1")
                            .sequenceNumber((short) 2)
                            .surname("Smith")
                            .forenames("John")
                            .build()
                    })
                    .build()
            )
            .build();
        legacy.setPartyDetails(party);
        var legacy2 = new uk.gov.hmcts.opal.dto.legacy.LegacyGetDefendantAccountHeaderSummaryResponse();
        legacy2.setPartyDetails(null);

        LegacyDefendantAccountService service = new LegacyDefendantAccountService(null, null);

        try {
            Method m = LegacyDefendantAccountService.class.getDeclaredMethod(
                "toHeaderSumaryDto",
                uk.gov.hmcts.opal.dto.legacy.LegacyGetDefendantAccountHeaderSummaryResponse.class
            );
            m.setAccessible(true);
            var dto = (DefendantAccountHeaderSummary) m.invoke(service, legacy);
            assertNotNull(dto);
            var dto2 = (DefendantAccountHeaderSummary) m.invoke(service, legacy2);
            assertNotNull(dto2);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testDefendantAccountService_ensureAuthenticated() throws Exception {
        Method m = DefendantAccountService.class.getDeclaredMethod("ensureAuthenticated", uk.gov.hmcts.opal.authorisation.model.UserState.class);
        m.setAccessible(true);
        try {
            m.invoke(null, (Object) null);
        } catch (Exception e) {
            assertTrue(e.getCause() instanceof org.springframework.security.authentication.AuthenticationCredentialsNotFoundException);
        }
    }

}
