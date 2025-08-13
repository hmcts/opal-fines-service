package uk.gov.hmcts.opal.service.legacy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.opal.dto.PostMinorCreditorAccountsSearchResponse;
import uk.gov.hmcts.opal.dto.legacy.CreditorAccount;
import uk.gov.hmcts.opal.dto.legacy.Defendant;
import uk.gov.hmcts.opal.dto.legacy.search.LegacyMinorCreditorSearchResultsResponse;
import uk.gov.hmcts.opal.entity.minorcreditor.MinorCreditorSearch;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.OK;

class LegacyMinorCreditorServiceTest {

    @Mock
    private GatewayService gatewayService;

    @InjectMocks
    private LegacyMinorCreditorService legacyMinorCreditorService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void searchMinorCreditors_shouldMapLegacyResponseToDto() {
        // Arrange
        MinorCreditorSearch search = MinorCreditorSearch.builder()
                .businessUnitIds(List.of(1))
                .accountNumber("ACC-1")
                .activeAccountsOnly(true)
                .build();

        Defendant defendant = Defendant.builder()
                .defendantAccountId("5L")
                .firstnames("Jane")
                .surname("Smith")
                .organisation(false)
                .organisationName(null)
                .build();

        CreditorAccount creditorAccount = CreditorAccount.builder()
                .creditorAccountId("2")
                .accountNumber("123456")
                .organisation(true)
                .organisationName("Org Ltd")
                .firstnames("John")
                .surname("Doe")
                .addressLine1("123 Road")
                .postcode("AB12 3CD")
                .businessUnitId("10L")
                .businessUnitName("Unit 10")
                .accountBalance(1000.00)
                .defendant(defendant)
                .build();

        LegacyMinorCreditorSearchResultsResponse legacyResponse = LegacyMinorCreditorSearchResultsResponse.builder()
                .count(1)
                .creditorAccounts(List.of(creditorAccount))
                .build();

        GatewayService.Response<LegacyMinorCreditorSearchResultsResponse> gatewayResponse =
                new GatewayService.Response<>(org.springframework.http.HttpStatus.OK, legacyResponse);

        when(gatewayService.postToGateway(
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.eq(LegacyMinorCreditorSearchResultsResponse.class),
                org.mockito.ArgumentMatchers.any())
        ).thenReturn(gatewayResponse);

        // Act
        PostMinorCreditorAccountsSearchResponse result = legacyMinorCreditorService.searchMinorCreditors(search);

        // Assert
        assertEquals(1, result.getCount());
        assertEquals(1, result.getCreditorAccounts().size());
        assertEquals("2", result.getCreditorAccounts().get(0).getCreditorAccountId());
        assertEquals("Jane", result.getCreditorAccounts().get(0).getDefendant().getFirstnames());
    }

    @Test
    void searchMinorCreditors_shouldReturnEmpty_whenLegacyResponseIsNull() {
        MinorCreditorSearch search = MinorCreditorSearch.builder().activeAccountsOnly(true).build();
        GatewayService.Response<LegacyMinorCreditorSearchResultsResponse> gatewayResponse =
                new GatewayService.Response<>(OK, (LegacyMinorCreditorSearchResultsResponse) null);

        when(gatewayService.postToGateway(
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.eq(LegacyMinorCreditorSearchResultsResponse.class),
                org.mockito.ArgumentMatchers.any())
        ).thenReturn(gatewayResponse);

        PostMinorCreditorAccountsSearchResponse result = legacyMinorCreditorService.searchMinorCreditors(search);

        assertEquals(0, result.getCount());
        assertTrue(result.getCreditorAccounts().isEmpty());
    }

    @Test
    void searchMinorCreditors_shouldMapMultipleCreditorAccounts() {
        MinorCreditorSearch search = MinorCreditorSearch.builder().activeAccountsOnly(true).build();

        CreditorAccount ca1 = CreditorAccount.builder()
                .creditorAccountId("1").accountNumber("A1").organisation(false)
                .firstnames("A").surname("B").addressLine1("Addr1").businessUnitId("BU1").businessUnitName("Unit1")
                .accountBalance(10.0).defendant(null).build();

        CreditorAccount ca2 = CreditorAccount.builder()
                .creditorAccountId("2").accountNumber("A2").organisation(true)
                .firstnames("C").surname("D").addressLine1("Addr2").businessUnitId("BU2").businessUnitName("Unit2")
                .accountBalance(20.0).defendant(null).build();

        LegacyMinorCreditorSearchResultsResponse legacyResponse = LegacyMinorCreditorSearchResultsResponse.builder()
                .count(2)
                .creditorAccounts(List.of(ca1, ca2))
                .build();

        GatewayService.Response<LegacyMinorCreditorSearchResultsResponse> gatewayResponse =
                new GatewayService.Response<>(OK, legacyResponse);

        when(gatewayService.postToGateway(
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.eq(LegacyMinorCreditorSearchResultsResponse.class),
                org.mockito.ArgumentMatchers.any())
        ).thenReturn(gatewayResponse);

        PostMinorCreditorAccountsSearchResponse result = legacyMinorCreditorService.searchMinorCreditors(search);

        assertEquals(2, result.getCount());
        assertEquals(2, result.getCreditorAccounts().size());
        assertEquals("1", result.getCreditorAccounts().get(0).getCreditorAccountId());
        assertEquals("2", result.getCreditorAccounts().get(1).getCreditorAccountId());
    }

    @Test
    void searchMinorCreditors_shouldHandleNullDefendant() {
        MinorCreditorSearch search = MinorCreditorSearch.builder().activeAccountsOnly(true).build();

        CreditorAccount ca = CreditorAccount.builder()
                .creditorAccountId("1").accountNumber("A1").organisation(false)
                .firstnames("A").surname("B").addressLine1("Addr1").businessUnitId("BU1").businessUnitName("Unit1")
                .accountBalance(10.0).defendant(null).build();

        LegacyMinorCreditorSearchResultsResponse legacyResponse = LegacyMinorCreditorSearchResultsResponse.builder()
                .count(1)
                .creditorAccounts(List.of(ca))
                .build();

        GatewayService.Response<LegacyMinorCreditorSearchResultsResponse> gatewayResponse =
                new GatewayService.Response<>(OK, legacyResponse);

        when(gatewayService.postToGateway(
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.eq(LegacyMinorCreditorSearchResultsResponse.class),
                org.mockito.ArgumentMatchers.any())
        ).thenReturn(gatewayResponse);

        PostMinorCreditorAccountsSearchResponse result = legacyMinorCreditorService.searchMinorCreditors(search);

        assertEquals(1, result.getCount());
        assertNull(result.getCreditorAccounts().get(0).getDefendant());
    }


}
