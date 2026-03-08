package uk.gov.hmcts.opal.service.legacy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.math.BigInteger;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.opal.dto.GetMinorCreditorAccountAtAGlanceResponse;
import uk.gov.hmcts.opal.dto.MinorCreditorSearch;
import uk.gov.hmcts.opal.dto.PostMinorCreditorAccountsSearchResponse;
import uk.gov.hmcts.opal.dto.legacy.CreditorAccount;
import uk.gov.hmcts.opal.dto.legacy.Defendant;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetMinorCreditorAccountAtAGlanceRequest;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetMinorCreditorAccountAtAGlanceResponse;
import uk.gov.hmcts.opal.dto.legacy.search.LegacyMinorCreditorSearchResultsResponse;
import uk.gov.hmcts.opal.generated.model.PatchMinorCreditorAccountRequest;
import uk.gov.hmcts.opal.mapper.response.GetMinorCreditorAccountAtAGlanceResponseMapper;

@ExtendWith(MockitoExtension.class)
class LegacyMinorCreditorServiceTest {

    @Mock
    private GatewayService gatewayService;

    @Mock
    private GetMinorCreditorAccountAtAGlanceResponseMapper atAGlanceResponseMapper;

    @InjectMocks
    private LegacyMinorCreditorService legacyMinorCreditorService;

    @Test
    void searchMinorCreditors_shouldMapLegacyResponseToDto() {
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

        GatewayService.Response<LegacyMinorCreditorSearchResultsResponse> response =
            new GatewayService.Response<>(HttpStatus.OK, legacyResponse, null, null);

        when(gatewayService.postToGateway(any(), eq(LegacyMinorCreditorSearchResultsResponse.class), any(), any()))
            .thenReturn(response);

        PostMinorCreditorAccountsSearchResponse result = legacyMinorCreditorService.searchMinorCreditors(search);

        assertEquals(1, result.getCount());
        assertEquals(1, result.getCreditorAccounts().size());
        assertEquals("2", result.getCreditorAccounts().getFirst().getCreditorAccountId());
        assertEquals("Jane", result.getCreditorAccounts().getFirst().getDefendant().getFirstnames());
    }

    @Test
    void searchMinorCreditors_shouldMapNullDefendantToNull() {
        MinorCreditorSearch search = MinorCreditorSearch.builder()
            .businessUnitIds(List.of(1))
            .accountNumber("ACC-2")
            .activeAccountsOnly(true)
            .build();

        CreditorAccount creditorAccount = CreditorAccount.builder()
            .creditorAccountId("3")
            .accountNumber("654321")
            .organisation(true)
            .organisationName("Org Ltd")
            .firstnames("John")
            .surname("Doe")
            .addressLine1("123 Road")
            .postcode("AB12 3CD")
            .businessUnitId("10L")
            .businessUnitName("Unit 10")
            .accountBalance(1000.00)
            .defendant(null)
            .build();

        LegacyMinorCreditorSearchResultsResponse legacyResponse = LegacyMinorCreditorSearchResultsResponse.builder()
            .count(1)
            .creditorAccounts(List.of(creditorAccount))
            .build();

        GatewayService.Response<LegacyMinorCreditorSearchResultsResponse> response =
            new GatewayService.Response<>(HttpStatus.OK, legacyResponse, null, null);

        when(gatewayService.postToGateway(
            any(),
            eq(LegacyMinorCreditorSearchResultsResponse.class),
            any(),
            any())
        ).thenReturn(response);

        PostMinorCreditorAccountsSearchResponse result = legacyMinorCreditorService.searchMinorCreditors(search);

        assertEquals(1, result.getCount());
        assertNull(result.getCreditorAccounts().getFirst().getDefendant());
    }

    @Test
    void searchMinorCreditors_shouldHandleGatewayException() {
        MinorCreditorSearch search = MinorCreditorSearch.builder().activeAccountsOnly(true).build();

        GatewayService.Response<LegacyMinorCreditorSearchResultsResponse> responseWithException =
            new GatewayService.Response<>(HttpStatus.INTERNAL_SERVER_ERROR,
                null, null, new RuntimeException("Gateway error"));

        when(gatewayService.postToGateway(any(), eq(LegacyMinorCreditorSearchResultsResponse.class), any(), any()))
            .thenReturn(responseWithException);

        PostMinorCreditorAccountsSearchResponse result = legacyMinorCreditorService.searchMinorCreditors(search);
        assertEquals(0, result.getCount());
        assertEquals(0, result.getCreditorAccounts().size());
    }

    @Test
    void searchMinorCreditors_shouldHandleLegacyFailure() {
        MinorCreditorSearch search = MinorCreditorSearch.builder().activeAccountsOnly(true).build();
        LegacyMinorCreditorSearchResultsResponse legacyResponse = LegacyMinorCreditorSearchResultsResponse.builder()
            .creditorAccounts(List.of())
            .build();

        GatewayService.Response<LegacyMinorCreditorSearchResultsResponse> response =
            new GatewayService.Response<>(HttpStatus.BAD_REQUEST, legacyResponse, null, null);

        when(gatewayService.postToGateway(any(), eq(LegacyMinorCreditorSearchResultsResponse.class), any(), any()))
            .thenReturn(response);

        PostMinorCreditorAccountsSearchResponse result = legacyMinorCreditorService.searchMinorCreditors(search);

        assertEquals(0, result.getCount());
        assertEquals(0, result.getCreditorAccounts().size());
    }

    @Test
    void searchMinorCreditors_shouldLogLegacyFailureEntity() {

        MinorCreditorSearch search = MinorCreditorSearch.builder().activeAccountsOnly(true).build();
        LegacyMinorCreditorSearchResultsResponse legacyResponse = LegacyMinorCreditorSearchResultsResponse.builder()
            .count(0)
            .creditorAccounts(List.of())
            .build();

        GatewayService.Response<LegacyMinorCreditorSearchResultsResponse> response =
            new GatewayService.Response<>(HttpStatus.INTERNAL_SERVER_ERROR, legacyResponse, "legacy failure", null);

        when(gatewayService.postToGateway(
            any(),
            eq(LegacyMinorCreditorSearchResultsResponse.class),
            any(),
            any())
        ).thenReturn(response);

        legacyMinorCreditorService.searchMinorCreditors(search);
    }

    @Test
    void getMinorCreditorAtAGlance_shouldCallResponseMapper() {
        // Arrange
        LegacyGetMinorCreditorAccountAtAGlanceResponse legacyResponse =
            LegacyGetMinorCreditorAccountAtAGlanceResponse.builder()
                .creditorAccountId(66L)
                .creditorAccountVersion(BigInteger.valueOf(1))
                .build();

        GatewayService.Response<LegacyGetMinorCreditorAccountAtAGlanceResponse> gatewayResponse =
            new GatewayService.Response<>(HttpStatus.OK, legacyResponse, null, null);

        when(gatewayService.postToGateway(any(), eq(LegacyGetMinorCreditorAccountAtAGlanceResponse.class),
            eq(LegacyGetMinorCreditorAccountAtAGlanceRequest.builder().creditorAccountId("happyPath").build()), any()))
            .thenReturn(gatewayResponse);

        GetMinorCreditorAccountAtAGlanceResponse mapperResponse = GetMinorCreditorAccountAtAGlanceResponse.builder()
            .creditorAccountId(66L)
            .build();

        when(atAGlanceResponseMapper.toDto(legacyResponse)).thenReturn(mapperResponse);

        // Act
        GetMinorCreditorAccountAtAGlanceResponse result = legacyMinorCreditorService
            .getMinorCreditorAtAGlance("happyPath");

        // Assert
        assertEquals(66L, result.getCreditorAccountId());
    }

    @Test
    void getMinorCreditorAtAGlance_shouldHandleGatewayException() {
        // Arrange
        LegacyGetMinorCreditorAccountAtAGlanceResponse legacyResponse =
            LegacyGetMinorCreditorAccountAtAGlanceResponse.builder()
                .creditorAccountId(66L)
                .creditorAccountVersion(BigInteger.valueOf(1))
                .build();

        GatewayService.Response<LegacyGetMinorCreditorAccountAtAGlanceResponse> responseWithException =
            new GatewayService.Response<>(HttpStatus.INTERNAL_SERVER_ERROR,
                legacyResponse, null, new RuntimeException("Gateway error"));

        when(gatewayService.postToGateway(any(), eq(LegacyGetMinorCreditorAccountAtAGlanceResponse.class),
            any(), any())).thenReturn(responseWithException);

        GetMinorCreditorAccountAtAGlanceResponse mapperResponse = GetMinorCreditorAccountAtAGlanceResponse.builder()
            .creditorAccountId(66L)
            .build();

        when(atAGlanceResponseMapper.toDto(legacyResponse)).thenReturn(mapperResponse);

        // Act
        GetMinorCreditorAccountAtAGlanceResponse result = legacyMinorCreditorService
            .getMinorCreditorAtAGlance("gatewayException");
    }

    @Test
    void getMinorCreditorAtAGlance_shouldHandleLegacyFailure() {
        // Arrange
        LegacyGetMinorCreditorAccountAtAGlanceResponse legacyResponse =
            LegacyGetMinorCreditorAccountAtAGlanceResponse.builder()
                .creditorAccountId(66L)
                .creditorAccountVersion(BigInteger.valueOf(1))
                .build();

        GatewayService.Response<LegacyGetMinorCreditorAccountAtAGlanceResponse> responseWithFailure =
            new GatewayService.Response<>(HttpStatus.INTERNAL_SERVER_ERROR,
                legacyResponse, null, null);

        when(gatewayService.postToGateway(any(), eq(LegacyGetMinorCreditorAccountAtAGlanceResponse.class),
            any(), any())).thenReturn(responseWithFailure);

        GetMinorCreditorAccountAtAGlanceResponse mapperResponse = GetMinorCreditorAccountAtAGlanceResponse.builder()
            .creditorAccountId(66L)
            .build();

        when(atAGlanceResponseMapper.toDto(legacyResponse)).thenReturn(mapperResponse);

        // Act
        GetMinorCreditorAccountAtAGlanceResponse result = legacyMinorCreditorService
            .getMinorCreditorAtAGlance("legacyFailure");
    }

    @Test
    void getHeaderSummary_shouldThrowUnsupportedOperationException() {
        UnsupportedOperationException exception = assertThrows(
            UnsupportedOperationException.class,
            () -> legacyMinorCreditorService.getHeaderSummary(1L)
        );

        assertEquals("Legacy mode not implemented for GET /minor-creditor-accounts/{id}/header-summary",
            exception.getMessage());
    }

    @Test
    void updateMinorCreditorAccount_shouldThrowUnsupportedOperationException() {
        UnsupportedOperationException exception = assertThrows(
            UnsupportedOperationException.class,
            () -> legacyMinorCreditorService.updateMinorCreditorAccount(
                1L,
                new PatchMinorCreditorAccountRequest(),
                BigInteger.ONE,
                "test.user")
        );

        assertEquals("Legacy mode not implemented for PATCH /minor-creditor-accounts/{id}", exception.getMessage());
    }
}
