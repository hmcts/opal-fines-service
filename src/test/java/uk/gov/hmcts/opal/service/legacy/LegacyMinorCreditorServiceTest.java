package uk.gov.hmcts.opal.service.legacy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.opal.dto.PostMinorCreditorAccountsSearchResponse;
import uk.gov.hmcts.opal.dto.legacy.CreditorAccount;
import uk.gov.hmcts.opal.dto.legacy.LegacyDefendant;
import uk.gov.hmcts.opal.dto.legacy.search.LegacyMinorCreditorSearchResultsResponse;
import uk.gov.hmcts.opal.dto.MinorCreditorSearch;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LegacyMinorCreditorServiceTest {

    @Mock
    private GatewayService gatewayService;

    @InjectMocks
    private LegacyMinorCreditorService legacyMinorCreditorService;

    @Test
    void searchMinorCreditors_shouldMapLegacyResponseToDto() {
        MinorCreditorSearch search = MinorCreditorSearch.builder()
                .businessUnitIds(List.of(1))
                .accountNumber("ACC-1")
                .activeAccountsOnly(true)
                .build();

        LegacyDefendant defendant = LegacyDefendant.builder()
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

        when(gatewayService.postToGateway(
                any(),
                eq(LegacyMinorCreditorSearchResultsResponse.class),
                any(),
                any())
        ).thenReturn(response);

        PostMinorCreditorAccountsSearchResponse result = legacyMinorCreditorService.searchMinorCreditors(search);

        assertEquals(1, result.getCount());
        assertEquals(1, result.getCreditorAccounts().size());
        assertEquals("2", result.getCreditorAccounts().get(0).getCreditorAccountId());
        assertEquals("Jane", result.getCreditorAccounts().get(0).getDefendant().getFirstnames());
    }

    @Test
    void searchMinorCreditors_shouldHandleGatewayException() {
        MinorCreditorSearch search = MinorCreditorSearch.builder().activeAccountsOnly(true).build();

        GatewayService.Response<LegacyMinorCreditorSearchResultsResponse> responseWithException =
                new GatewayService.Response<>(HttpStatus.INTERNAL_SERVER_ERROR,
                        null, null, new RuntimeException("Gateway error"));

        when(gatewayService.postToGateway(
                any(),
                eq(LegacyMinorCreditorSearchResultsResponse.class),
                any(),
                any())
        ).thenReturn(responseWithException);

        PostMinorCreditorAccountsSearchResponse result = legacyMinorCreditorService.searchMinorCreditors(search);
        assertEquals(0, result.getCount());
        assertEquals(0, result.getCreditorAccounts().size());
    }

    @Test
    void searchMinorCreditors_shouldHandleLegacyFailure() {
        MinorCreditorSearch search = MinorCreditorSearch.builder().activeAccountsOnly(true).build();
        LegacyMinorCreditorSearchResultsResponse legacyResponse = LegacyMinorCreditorSearchResultsResponse.builder()
                .count(0)
                .creditorAccounts(List.of())
                .build();

        GatewayService.Response<LegacyMinorCreditorSearchResultsResponse> response =
                new GatewayService.Response<>(HttpStatus.BAD_REQUEST, legacyResponse, null, null);

        when(gatewayService.postToGateway(
                any(),
                eq(LegacyMinorCreditorSearchResultsResponse.class),
                any(),
                any())
        ).thenReturn(response);

        PostMinorCreditorAccountsSearchResponse result = legacyMinorCreditorService.searchMinorCreditors(search);

        assertEquals(0, result.getCount());
        assertEquals(0, result.getCreditorAccounts().size());
    }
}
