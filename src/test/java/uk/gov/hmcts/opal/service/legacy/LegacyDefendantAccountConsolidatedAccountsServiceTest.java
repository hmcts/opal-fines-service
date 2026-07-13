package uk.gov.hmcts.opal.service.legacy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import java.io.StringReader;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.opal.common.legacy.config.LegacyGatewayProperties;
import uk.gov.hmcts.opal.common.legacy.service.GatewayService;
import uk.gov.hmcts.opal.dto.GetDefendantAccountConsolidatedAccountsResult;
import uk.gov.hmcts.opal.dto.legacy.LegacyConsolidatedAccount;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetDefendantAccountConsolidatedAccountsResponse;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetDefendantAccountRequest;
import uk.gov.hmcts.opal.generated.model.ConsolidatedAccountDefendantAccount;
import uk.gov.hmcts.opal.mapper.legacy.DefendantAccountHistoryLegacyResponseMapper;
import uk.gov.hmcts.opal.mapper.legacy.LegacyUpdateDefendantAccountResponseMapper;
import uk.gov.hmcts.opal.mapper.request.UpdateDefendantAccountRequestMapper;
import uk.gov.hmcts.opal.service.opal.CourtService;
import uk.gov.hmcts.opal.service.opal.LocalJusticeAreaService;
import uk.gov.hmcts.opal.service.opal.history.HistoryItemOrderingService;

@ExtendWith(MockitoExtension.class)
class LegacyDefendantAccountConsolidatedAccountsServiceTest {

    @Mock
    private GatewayService gatewayService;

    @Mock
    private LegacyGatewayProperties legacyGatewayProperties;

    @Mock
    private CourtService courtService;

    @Mock
    private LocalJusticeAreaService ljaService;

    @Mock
    private HistoryItemOrderingService historyItemOrderingService;

    @Mock
    private DefendantAccountHistoryLegacyResponseMapper legacyDefendantAccountHistoryResponseMapper;

    @Mock
    private UpdateDefendantAccountRequestMapper updateDefendantAccountRequestMapper;

    @Mock
    private LegacyUpdateDefendantAccountResponseMapper legacyUpdateDefendantAccountResponseMapper;

    @InjectMocks
    private LegacyDefendantAccountService legacyDefendantAccountService;

    @Test
    void getConsolidatedAccounts_postsLegacyRequestAndMapsSortedResponse() {
        LegacyGetDefendantAccountConsolidatedAccountsResponse legacyResponse =
            LegacyGetDefendantAccountConsolidatedAccountsResponse.builder()
                .version(7L)
                .consolidatedAccounts(List.of(
                    legacyAccount(233302L, "233302C"),
                    legacyAccount(233301L, "233301C")
                ))
                .build();

        ArgumentCaptor<LegacyGetDefendantAccountRequest> requestCaptor =
            ArgumentCaptor.forClass(LegacyGetDefendantAccountRequest.class);

        when(gatewayService.postToGateway(
            eq(LegacyDefendantAccountService.GET_CONSOLIDATED_ACCOUNTS),
            eq(LegacyGetDefendantAccountConsolidatedAccountsResponse.class),
            requestCaptor.capture(),
            isNull()
        )).thenReturn(new GatewayService.Response<>(HttpStatus.OK, legacyResponse, null, null));

        GetDefendantAccountConsolidatedAccountsResult response =
            legacyDefendantAccountService.getConsolidatedAccounts(233300L);

        verify(gatewayService).postToGateway(
            eq(LegacyDefendantAccountService.GET_CONSOLIDATED_ACCOUNTS),
            eq(LegacyGetDefendantAccountConsolidatedAccountsResponse.class),
            eq(requestCaptor.getValue()),
            isNull()
        );

        assertEquals("233300", requestCaptor.getValue().getDefendantAccountId());
        assertEquals(BigInteger.valueOf(7), response.getVersion());
        assertNotNull(response.getPayload());
        assertEquals(2, response.getPayload().size());

        ConsolidatedAccountDefendantAccount first = response.getPayload().getFirst();
        assertEquals(233301L, first.getAccountId());
        assertEquals("233301C", first.getAccountNumber());
        assertEquals("Alex", first.getFirstName());
        assertEquals("Jones", first.getLastName());
        assertEquals(LocalDate.parse("2026-01-21"), first.getDateImposed());
        assertEquals("Child Court", first.getImposedBy());
        assertEquals("CHILD-REF", first.getReference());
    }

    @Test
    void getConsolidatedAccounts_whenGatewayReturnsEmptyList_returnsEmptyPayload() {
        LegacyGetDefendantAccountConsolidatedAccountsResponse legacyResponse =
            LegacyGetDefendantAccountConsolidatedAccountsResponse.builder()
                .version(1L)
                .consolidatedAccounts(List.of())
                .build();

        when(gatewayService.postToGateway(
            eq(LegacyDefendantAccountService.GET_CONSOLIDATED_ACCOUNTS),
            eq(LegacyGetDefendantAccountConsolidatedAccountsResponse.class),
            eq(LegacyGetDefendantAccountRequest.builder().defendantAccountId("233300").build()),
            isNull()
        )).thenReturn(new GatewayService.Response<>(HttpStatus.OK, legacyResponse, null, null));

        GetDefendantAccountConsolidatedAccountsResult response =
            legacyDefendantAccountService.getConsolidatedAccounts(233300L);

        assertEquals(BigInteger.ONE, response.getVersion());
        assertEquals(List.of(), response.getPayload());
    }

    @Test
    void getConsolidatedAccounts_legacyXmlResponseUnmarshalsAndMapsToOpalResponse() throws JAXBException {
        String legacyXml = """
            <response>
              <version>3</version>
              <consolidatedAccounts>
                <consolidatedAccounts_element>
                  <accountId>233302</accountId>
                  <accountNumber>233302C</accountNumber>
                  <firstName>Alex</firstName>
                  <lastName>Jones</lastName>
                  <dateImposed>2026-01-21</dateImposed>
                  <imposedBy>Child Court</imposedBy>
                  <reference>CHILD-REF</reference>
                </consolidatedAccounts_element>
              </consolidatedAccounts>
            </response>
            """;
        LegacyGetDefendantAccountConsolidatedAccountsResponse legacyResponse =
            (LegacyGetDefendantAccountConsolidatedAccountsResponse) JAXBContext
                .newInstance(LegacyGetDefendantAccountConsolidatedAccountsResponse.class)
                .createUnmarshaller()
                .unmarshal(new StringReader(legacyXml));

        when(gatewayService.postToGateway(
            eq(LegacyDefendantAccountService.GET_CONSOLIDATED_ACCOUNTS),
            eq(LegacyGetDefendantAccountConsolidatedAccountsResponse.class),
            eq(LegacyGetDefendantAccountRequest.builder().defendantAccountId("233300").build()),
            isNull()
        )).thenReturn(new GatewayService.Response<>(HttpStatus.OK, legacyResponse, null, null));

        GetDefendantAccountConsolidatedAccountsResult response =
            legacyDefendantAccountService.getConsolidatedAccounts(233300L);

        assertEquals(BigInteger.valueOf(3), response.getVersion());
        assertEquals(1, response.getPayload().size());

        ConsolidatedAccountDefendantAccount account = response.getPayload().getFirst();
        assertEquals(233302L, account.getAccountId());
        assertEquals("233302C", account.getAccountNumber());
        assertEquals("Alex", account.getFirstName());
        assertEquals("Jones", account.getLastName());
        assertEquals(LocalDate.parse("2026-01-21"), account.getDateImposed());
        assertEquals("Child Court", account.getImposedBy());
        assertEquals("CHILD-REF", account.getReference());
    }

    private LegacyConsolidatedAccount legacyAccount(Long accountId, String accountNumber) {
        return LegacyConsolidatedAccount.builder()
            .accountId(accountId)
            .accountNumber(accountNumber)
            .firstName("Alex")
            .lastName("Jones")
            .dateImposed(LocalDate.parse("2026-01-21"))
            .imposedBy("Child Court")
            .reference("CHILD-REF")
            .build();
    }
}
