package uk.gov.hmcts.opal.service.legacy;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import java.math.BigInteger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.opal.dto.GetMinorCreditorAccountAtAGlanceResponse;
import uk.gov.hmcts.opal.dto.PostMinorCreditorAccountsSearchResponse;
import uk.gov.hmcts.opal.dto.legacy.CreditorAccount;
import uk.gov.hmcts.opal.dto.legacy.Defendant;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetMinorCreditorAccountAtAGlanceRequest;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetMinorCreditorAccountAtAGlanceResponse;
import uk.gov.hmcts.opal.dto.legacy.search.LegacyMinorCreditorSearchResultsResponse;
import uk.gov.hmcts.opal.dto.MinorCreditorSearch;

import java.util.List;
import uk.gov.hmcts.opal.mapper.response.GetMinorCreditorAccountAtAGlanceResponseMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

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

        when(gatewayService.postToGateway(
                any(),
                eq(LegacyMinorCreditorSearchResultsResponse.class),
                any(),
                any())
        ).thenReturn(response);

        PostMinorCreditorAccountsSearchResponse result = legacyMinorCreditorService.searchMinorCreditors(search);

        assertEquals(1, result.getCount());
        assertEquals(1, result.getCreditorAccounts().size());
        assertEquals("2", result.getCreditorAccounts().getFirst().getCreditorAccountId());
        assertEquals("Jane", result.getCreditorAccounts().getFirst().getDefendant().getFirstnames());
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

    @Test
    void getMinorCreditorAtAGlance_shouldCallResponseMapper() {
        // Arrange
        LegacyGetMinorCreditorAccountAtAGlanceResponse legacyResponse =
            LegacyGetMinorCreditorAccountAtAGlanceResponse.builder()
                .party(null)
                .address(null)
                .creditorAccountId(66L)
                .creditorAccountVersion(BigInteger.valueOf(1))
                .defendant(null)
                .payment(null)
                .errorResponse(null)
                .build();

        GatewayService.Response<LegacyGetMinorCreditorAccountAtAGlanceResponse> gatewayResponse =
            new GatewayService.Response<>(HttpStatus.OK, legacyResponse, null, null);

        when(gatewayService.postToGateway(
            any(),
            eq(LegacyGetMinorCreditorAccountAtAGlanceResponse.class),
            eq(LegacyGetMinorCreditorAccountAtAGlanceRequest.builder().creditorAccountId("happyPath").build()),
            any())
        ).thenReturn(gatewayResponse);

        GetMinorCreditorAccountAtAGlanceResponse mapperResponse =
            GetMinorCreditorAccountAtAGlanceResponse.builder()
                .party(null)
                .address(null)
                .creditorAccountId(66L)
                .defendant(null)
                .payment(null)
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
        Logger logger = (Logger) LoggerFactory.getLogger("opal.LegacyMinorCreditorService");

        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);

        LegacyGetMinorCreditorAccountAtAGlanceResponse legacyResponse =
            LegacyGetMinorCreditorAccountAtAGlanceResponse.builder()
                .party(null)
                .address(null)
                .creditorAccountId(66L)
                .creditorAccountVersion(BigInteger.valueOf(1))
                .defendant(null)
                .payment(null)
                .errorResponse(null)
                .build();

        GatewayService.Response<LegacyGetMinorCreditorAccountAtAGlanceResponse> responseWithException =
            new GatewayService.Response<>(HttpStatus.INTERNAL_SERVER_ERROR,
                legacyResponse, null, new RuntimeException("Gateway error"));

        when(gatewayService.postToGateway(
            any(),
            eq(LegacyGetMinorCreditorAccountAtAGlanceResponse.class),
            any(),
            any())
        ).thenReturn(responseWithException);

        GetMinorCreditorAccountAtAGlanceResponse mapperResponse =
            GetMinorCreditorAccountAtAGlanceResponse.builder()
                .party(null)
                .address(null)
                .creditorAccountId(66L)
                .defendant(null)
                .payment(null)
                .build();

        when(atAGlanceResponseMapper.toDto(legacyResponse)).thenReturn(mapperResponse);

        // Act
        legacyMinorCreditorService.getMinorCreditorAtAGlance("gatewayException");

        // Assert
        List<ILoggingEvent> logs = listAppender.list;

        assertEquals(2, logs.size());
        assertEquals(Level.ERROR, logs.getFirst().getLevel());
        assertEquals(":getMinorCreditorAtAGlance: Legacy Gateway response: HTTP Response Code: "
            + "500 INTERNAL_SERVER_ERROR", logs.getFirst().getFormattedMessage());
        assertEquals(Level.ERROR, logs.get(1).getLevel());
        assertEquals(":getMinorCreditorAtAGlance: Exception Message: Gateway error", logs.get(1).getFormattedMessage());

        logger.detachAppender(listAppender);
        listAppender.stop();
    }

    @Test
    void getMinorCreditorAtAGlance_shouldHandleLegacyFailure() {
        // Arrange
        Logger logger = (Logger) LoggerFactory.getLogger("opal.LegacyMinorCreditorService");

        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);

        LegacyGetMinorCreditorAccountAtAGlanceResponse legacyResponse =
            LegacyGetMinorCreditorAccountAtAGlanceResponse.builder()
                .party(null)
                .address(null)
                .creditorAccountId(66L)
                .creditorAccountVersion(BigInteger.valueOf(1))
                .defendant(null)
                .payment(null)
                .errorResponse(null)
                .build();

        GatewayService.Response<LegacyGetMinorCreditorAccountAtAGlanceResponse> responseWithFailure =
            new GatewayService.Response<>(HttpStatus.INTERNAL_SERVER_ERROR,
                legacyResponse, null, null);

        when(gatewayService.postToGateway(
            any(),
            eq(LegacyGetMinorCreditorAccountAtAGlanceResponse.class),
            any(),
            any())
        ).thenReturn(responseWithFailure);

        GetMinorCreditorAccountAtAGlanceResponse mapperResponse =
            GetMinorCreditorAccountAtAGlanceResponse.builder()
                .party(null)
                .address(null)
                .creditorAccountId(66L)
                .defendant(null)
                .payment(null)
                .build();

        when(atAGlanceResponseMapper.toDto(legacyResponse)).thenReturn(mapperResponse);

        // Act
        GetMinorCreditorAccountAtAGlanceResponse result = legacyMinorCreditorService
            .getMinorCreditorAtAGlance("legacyFailure");

        // Assert
        List<ILoggingEvent> logs = listAppender.list;

        assertEquals(3, logs.size());
        assertEquals(Level.ERROR, logs.getFirst().getLevel());
        assertEquals(":getMinorCreditorAtAGlance: Legacy Gateway response: HTTP Response Code: "
            + "500 INTERNAL_SERVER_ERROR", logs.getFirst().getFormattedMessage());
        assertEquals(Level.ERROR, logs.get(1).getLevel());
        assertEquals(":getMinorCreditorAtAGlance: Legacy Gateway: body: \n"
            + "null", logs.get(1).getFormattedMessage());
        assertEquals(Level.ERROR, logs.get(2).getLevel());
        assertEquals(":getMinorCreditorAtAGlance: Legacy Gateway: entity: \n"
            + "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
            + "<response>\n"
            + "    <creditor_account_id>66</creditor_account_id>\n"
            + "    <creditor_account_version>1</creditor_account_version>\n"
            + "</response>\n", logs.get(2).getFormattedMessage());

        logger.detachAppender(listAppender);
        listAppender.stop();
    }
}
