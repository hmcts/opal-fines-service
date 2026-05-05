package uk.gov.hmcts.opal.service.legacy;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.opal.common.legacy.service.GatewayService;
import uk.gov.hmcts.opal.dto.GetMinorCreditorAccountAtAGlanceResponse;
import uk.gov.hmcts.opal.dto.GetMinorCreditorAccountHeaderSummaryResponse;
import uk.gov.hmcts.opal.dto.MinorCreditorAccountResponse;
import uk.gov.hmcts.opal.dto.MinorCreditorSearch;
import uk.gov.hmcts.opal.dto.PostMinorCreditorAccountsSearchResponse;
import uk.gov.hmcts.opal.dto.legacy.CreditorAccount;
import uk.gov.hmcts.opal.dto.legacy.Defendant;
import uk.gov.hmcts.opal.dto.legacy.GetMinorCreditorAccountHeaderSummaryLegacyRequest;
import uk.gov.hmcts.opal.dto.legacy.GetMinorCreditorAccountHeaderSummaryLegacyResponse;
import uk.gov.hmcts.opal.dto.legacy.GetMinorCreditorAccountHeaderSummaryLegacyResponse.CreditorHeaderLegacy;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetMinorCreditorAccountAtAGlanceRequest;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetMinorCreditorAccountAtAGlanceResponse;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetMinorCreditorAccountRequest;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetMinorCreditorAccountResponse;
import uk.gov.hmcts.opal.dto.legacy.LegacyUpdateMinorCreditorAccountRequest;
import uk.gov.hmcts.opal.dto.legacy.LegacyUpdateMinorCreditorAccountResponse;
import uk.gov.hmcts.opal.dto.legacy.common.LegacyCreditorAccountPaymentDetails;
import uk.gov.hmcts.opal.dto.legacy.common.LegacyPartyDetails;
import uk.gov.hmcts.opal.dto.legacy.search.LegacyMinorCreditorSearchResultsResponse;
import uk.gov.hmcts.opal.entity.creditoraccount.CreditorAccountEntity;
import uk.gov.hmcts.opal.generated.model.PatchMinorCreditorAccountRequest;
import uk.gov.hmcts.opal.mapper.legacy.GetMinorCreditorAccountHeaderSummaryResponseLegacyMapper;
import uk.gov.hmcts.opal.mapper.legacy.LegacyMinorCreditorAccountResponseMapper;
import uk.gov.hmcts.opal.mapper.legacy.LegacyUpdateMinorCreditorAccountResponseMapper;
import uk.gov.hmcts.opal.mapper.request.UpdateMinorCreditorAccountRequestMapper;
import uk.gov.hmcts.opal.mapper.response.GetMinorCreditorAccountAtAGlanceResponseMapper;
import uk.gov.hmcts.opal.repository.CreditorAccountRepository;

@ExtendWith(MockitoExtension.class)
class LegacyMinorCreditorServiceTest {

    @Mock
    private GatewayService gatewayService;

    @Mock
    private GetMinorCreditorAccountAtAGlanceResponseMapper atAGlanceResponseMapper;

    @Mock
    private GetMinorCreditorAccountHeaderSummaryResponseLegacyMapper headerSummaryResponseMapper;

    @Mock
    private LegacyMinorCreditorAccountResponseMapper minorCreditorAccountResponseMapper;

    @Mock
    private CreditorAccountRepository creditorAccountRepository;

    @Mock
    private UpdateMinorCreditorAccountRequestMapper updateMinorCreditorAccountRequestMapper;

    @Mock
    private LegacyUpdateMinorCreditorAccountResponseMapper updateMinorCreditorAccountResponseMapper;

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
        LegacyGetMinorCreditorAccountAtAGlanceResponse legacyResponse =
            LegacyGetMinorCreditorAccountAtAGlanceResponse.builder()
                .creditorAccountId(66L)
                .creditorAccountVersion(BigInteger.valueOf(1))
                .build();

        GatewayService.Response<LegacyGetMinorCreditorAccountAtAGlanceResponse> gatewayResponse =
            new GatewayService.Response<>(HttpStatus.OK, legacyResponse, null, null);

        when(gatewayService.postToGateway(
            any(),
            eq(LegacyGetMinorCreditorAccountAtAGlanceResponse.class),
            eq(LegacyGetMinorCreditorAccountAtAGlanceRequest.builder().creditorAccountId("101").build()),
            any())
        ).thenReturn(gatewayResponse);

        GetMinorCreditorAccountAtAGlanceResponse mapperResponse = GetMinorCreditorAccountAtAGlanceResponse.builder()
            .creditorAccountId(66L)
            .build();

        when(atAGlanceResponseMapper.toDto(legacyResponse)).thenReturn(mapperResponse);

        GetMinorCreditorAccountAtAGlanceResponse result = legacyMinorCreditorService
            .getMinorCreditorAtAGlance(101L);

        assertEquals(66L, result.getCreditorAccountId());
    }

    @Test
    void getMinorCreditorAtAGlance_shouldHandleGatewayException() {
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

        GetMinorCreditorAccountAtAGlanceResponse result =
            assertDoesNotThrow(() -> legacyMinorCreditorService.getMinorCreditorAtAGlance(101L));

        assertSame(mapperResponse, result);

        verify(gatewayService, times(1)).postToGateway(
            any(),
            eq(LegacyGetMinorCreditorAccountAtAGlanceResponse.class),
            any(),
            any()
        );
        verify(atAGlanceResponseMapper, times(1)).toDto(legacyResponse);
        verifyNoMoreInteractions(gatewayService, atAGlanceResponseMapper);
    }

    @Test
    void getMinorCreditorAtAGlance_shouldHandleLegacyFailure() {
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

        GetMinorCreditorAccountAtAGlanceResponse result =
            assertDoesNotThrow(() -> legacyMinorCreditorService.getMinorCreditorAtAGlance(101L));

        assertSame(mapperResponse, result);

        verify(gatewayService, times(1)).postToGateway(
            any(),
            eq(LegacyGetMinorCreditorAccountAtAGlanceResponse.class),
            any(),
            any()
        );
        verify(atAGlanceResponseMapper, times(1)).toDto(legacyResponse);
        verifyNoMoreInteractions(gatewayService, atAGlanceResponseMapper);
    }

    @Test
    void getMinorCreditorAccount_shouldMapLegacyResponseToDto() {
        LegacyGetMinorCreditorAccountResponse legacyResponse = LegacyGetMinorCreditorAccountResponse.builder()
            .creditorAccountId(101L)
            .accountVersion(7L)
            .partyDetails(LegacyPartyDetails.builder().build())
            .payment(LegacyCreditorAccountPaymentDetails.builder().payByBacs(true).holdPayment(false).build())
            .build();

        GatewayService.Response<LegacyGetMinorCreditorAccountResponse> gatewayResponse =
            new GatewayService.Response<>(HttpStatus.OK, legacyResponse, null, null);

        MinorCreditorAccountResponse mappedResponse = new MinorCreditorAccountResponse();
        mappedResponse.setCreditorAccountId(101L);

        when(gatewayService.postToGateway(
            eq("GET_MINOR_CREDITOR_ACCOUNT_PARTY"),
            eq(LegacyGetMinorCreditorAccountResponse.class),
            eq(LegacyGetMinorCreditorAccountRequest.builder().accountId("101").build()),
            any()
        )).thenReturn(gatewayResponse);
        when(minorCreditorAccountResponseMapper.toMinorCreditorAccountResponse(legacyResponse))
            .thenReturn(mappedResponse);
        when(creditorAccountRepository.findById(101L)).thenReturn(
            Optional.of(CreditorAccountEntity.builder().businessUnitId((short) 77).build())
        );

        MinorCreditorAccountResponse result = legacyMinorCreditorService.getMinorCreditorAccount(101L);

        assertEquals(101L, result.getCreditorAccountId());
        assertEquals((short) 77, result.getBusinessUnitId());
    }

    @Test
    void getHeaderSummary_shouldCallResponseMapper() {
        GetMinorCreditorAccountHeaderSummaryLegacyResponse legacyResponse =
            GetMinorCreditorAccountHeaderSummaryLegacyResponse.builder()
                .creditor(CreditorHeaderLegacy.builder().accountVersion(1).build())
                .build();

        GatewayService.Response<GetMinorCreditorAccountHeaderSummaryLegacyResponse> gatewayResponse =
            new GatewayService.Response<>(HttpStatus.OK, legacyResponse, null, null);

        when(gatewayService.postToGateway(
            any(),
            eq(GetMinorCreditorAccountHeaderSummaryLegacyResponse.class),
            eq(GetMinorCreditorAccountHeaderSummaryLegacyRequest.builder().creditorAccountId("101").build()),
            any())
        ).thenReturn(gatewayResponse);

        GetMinorCreditorAccountHeaderSummaryResponse mapperResponse =
            GetMinorCreditorAccountHeaderSummaryResponse.builder()
                .creditor(GetMinorCreditorAccountHeaderSummaryResponse.CreditorHeader.builder()
                    .accountId("101")
                    .build())
                .build();

        when(headerSummaryResponseMapper.toOpal(legacyResponse)).thenReturn(mapperResponse);

        GetMinorCreditorAccountHeaderSummaryResponse result = legacyMinorCreditorService.getHeaderSummary(101L);

        assertEquals("101", result.getCreditor().getAccountId());
        assertEquals(BigInteger.ONE, result.getVersion());
    }

    @Test
    void getHeaderSummary_shouldHandleGatewayException() {
        GetMinorCreditorAccountHeaderSummaryLegacyResponse legacyResponse =
            GetMinorCreditorAccountHeaderSummaryLegacyResponse.builder()
                .creditor(CreditorHeaderLegacy.builder().accountVersion(1).build())
                .build();

        GatewayService.Response<GetMinorCreditorAccountHeaderSummaryLegacyResponse> responseWithException =
            new GatewayService.Response<>(HttpStatus.INTERNAL_SERVER_ERROR,
                legacyResponse, null, new RuntimeException("Gateway error"));

        when(gatewayService.postToGateway(any(), eq(GetMinorCreditorAccountHeaderSummaryLegacyResponse.class),
            any(), any())).thenReturn(responseWithException);

        GetMinorCreditorAccountHeaderSummaryResponse mapperResponse =
            GetMinorCreditorAccountHeaderSummaryResponse.builder().build();

        when(headerSummaryResponseMapper.toOpal(legacyResponse)).thenReturn(mapperResponse);

        GetMinorCreditorAccountHeaderSummaryResponse result =
            assertDoesNotThrow(() -> legacyMinorCreditorService.getHeaderSummary(101L));

        assertSame(mapperResponse, result);

        verify(gatewayService, times(1)).postToGateway(
            any(),
            eq(GetMinorCreditorAccountHeaderSummaryLegacyResponse.class),
            any(),
            any()
        );
        verify(headerSummaryResponseMapper, times(1)).toOpal(legacyResponse);
        verifyNoMoreInteractions(gatewayService, headerSummaryResponseMapper);
    }

    @Test
    void getHeaderSummary_shouldHandleLegacyFailure() {
        GetMinorCreditorAccountHeaderSummaryLegacyResponse legacyResponse =
            GetMinorCreditorAccountHeaderSummaryLegacyResponse.builder()
                .creditor(CreditorHeaderLegacy.builder().accountVersion(1).build())
                .build();

        GatewayService.Response<GetMinorCreditorAccountHeaderSummaryLegacyResponse> responseWithFailure =
            new GatewayService.Response<>(HttpStatus.INTERNAL_SERVER_ERROR,
                legacyResponse, null, null);

        when(gatewayService.postToGateway(any(), eq(GetMinorCreditorAccountHeaderSummaryLegacyResponse.class),
            any(), any())).thenReturn(responseWithFailure);

        GetMinorCreditorAccountHeaderSummaryResponse mapperResponse =
            GetMinorCreditorAccountHeaderSummaryResponse.builder().build();

        when(headerSummaryResponseMapper.toOpal(legacyResponse)).thenReturn(mapperResponse);

        GetMinorCreditorAccountHeaderSummaryResponse result =
            assertDoesNotThrow(() -> legacyMinorCreditorService.getHeaderSummary(101L));

        assertSame(mapperResponse, result);

        verify(gatewayService, times(1)).postToGateway(
            any(),
            eq(GetMinorCreditorAccountHeaderSummaryLegacyResponse.class),
            any(),
            any()
        );
        verify(headerSummaryResponseMapper, times(1)).toOpal(legacyResponse);
        verifyNoMoreInteractions(gatewayService, headerSummaryResponseMapper);
    }

    @Test
    void updateMinorCreditorAccount_shouldMapRequestCallGatewayAndReturnMappedResponse() {
        PatchMinorCreditorAccountRequest request = new PatchMinorCreditorAccountRequest();
        LegacyUpdateMinorCreditorAccountRequest legacyRequest = LegacyUpdateMinorCreditorAccountRequest.builder()
            .creditorAccountId("1")
            .businessUnitId("77")
            .businessUnitUserId("test.user")
            .accountVersion(1)
            .build();
        LegacyUpdateMinorCreditorAccountResponse legacyResponse = LegacyUpdateMinorCreditorAccountResponse.builder()
            .accountVersion(2)
            .creditorAccountId(1L)
            .partyDetails(new LegacyPartyDetails())
            .address(new uk.gov.hmcts.opal.dto.legacy.AddressDetailsLegacy())
            .payment(new LegacyCreditorAccountPaymentDetails())
            .build();
        GatewayService.Response<LegacyUpdateMinorCreditorAccountResponse> gatewayResponse =
            new GatewayService.Response<>(HttpStatus.OK, legacyResponse, null, null);
        MinorCreditorAccountResponse mappedResponse = new MinorCreditorAccountResponse();

        when(updateMinorCreditorAccountRequestMapper.toLegacyUpdateMinorCreditorAccountRequest(
            1L,
            (short) 77,
            "test.user",
            BigInteger.ONE,
            request
        )).thenReturn(legacyRequest);
        when(gatewayService.postToGateway(
            "LIBRA.of_update_minor_creditor_account",
            LegacyUpdateMinorCreditorAccountResponse.class,
            legacyRequest,
            null
        )).thenReturn(gatewayResponse);
        when(updateMinorCreditorAccountResponseMapper.toMinorCreditorAccountResponse(legacyResponse))
            .thenReturn(mappedResponse);

        MinorCreditorAccountResponse result = legacyMinorCreditorService.updateMinorCreditorAccount(
            1L,
            request,
            BigInteger.ONE,
            "test.user",
            (short) 77
        );

        assertSame(mappedResponse, result);
    }
}
