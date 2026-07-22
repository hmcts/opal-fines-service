package uk.gov.hmcts.opal.service.legacy;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import uk.gov.hmcts.opal.common.legacy.service.GatewayService;
import uk.gov.hmcts.opal.common.legacy.service.GatewayService.Response;
import uk.gov.hmcts.opal.dto.GetMajorCreditorAccountAtAGlanceResponse;
import uk.gov.hmcts.opal.dto.GetMajorCreditorAccountHeaderSummaryResponse;
import uk.gov.hmcts.opal.dto.history.HistoryItemType;
import uk.gov.hmcts.opal.dto.legacy.GetMajorCreditorAccountHistoryLegacyRequest;
import uk.gov.hmcts.opal.dto.legacy.GetMajorCreditorAccountHistoryLegacyResponse;
import uk.gov.hmcts.opal.dto.legacy.GetMajorCreditorAccountAtAGlanceLegacyRequest;
import uk.gov.hmcts.opal.dto.legacy.GetMajorCreditorAccountAtAGlanceLegacyResponse;
import uk.gov.hmcts.opal.dto.legacy.GetMajorCreditorAccountHeaderSummaryLegacyRequest;
import uk.gov.hmcts.opal.dto.legacy.GetMajorCreditorAccountHeaderSummaryLegacyResponse;
import uk.gov.hmcts.opal.dto.legacy.GetMajorCreditorAccountHeaderSummaryLegacyResponse.MajorCreditorLegacy;
import uk.gov.hmcts.opal.dto.response.GetMajorCreditorHistoryResponse;
import uk.gov.hmcts.opal.mapper.legacy.GetMajorCreditorAccountHistoryResponseLegacyMapper;
import uk.gov.hmcts.opal.mapper.legacy.GetMajorCreditorAccountAtAGlanceResponseLegacyMapper;
import uk.gov.hmcts.opal.mapper.legacy.GetMajorCreditorAccountHeaderSummaryResponseLegacyMapper;
import uk.gov.hmcts.opal.service.iface.MajorCreditorAccountServiceInterface;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "opal.LegacyMajorCreditorAccountService")
public class LegacyMajorCreditorAccountService implements MajorCreditorAccountServiceInterface {

    public static final String GET_MAJOR_CREDITOR_ACCOUNT_AT_A_GLANCE =
        "LIBRA.get_major_creditor_account_at_a_glance";
    public static final String GET_MAJOR_CREDITOR_ACCOUNT_HEADER_SUMMARY =
        "LIBRA.get_major_creditor_account_header_summary";
    public static final String GET_MAJOR_CREDITOR_ACCOUNT_HISTORY =
        "LIBRA.get_major_creditor_account_history";

    private final GatewayService gatewayService;
    private final GetMajorCreditorAccountAtAGlanceResponseLegacyMapper atAGlanceResponseMapper;
    private final GetMajorCreditorAccountHeaderSummaryResponseLegacyMapper headerSummaryResponseMapper;
    private final GetMajorCreditorAccountHistoryResponseLegacyMapper historyResponseMapper;

    @Override
    public GetMajorCreditorAccountAtAGlanceResponse getAtAGlance(Long majorCreditorAccountId) {
        Response<GetMajorCreditorAccountAtAGlanceLegacyResponse> response =
            gatewayService.postToGateway(
                GET_MAJOR_CREDITOR_ACCOUNT_AT_A_GLANCE,
                GetMajorCreditorAccountAtAGlanceLegacyResponse.class,
                GetMajorCreditorAccountAtAGlanceLegacyRequest.builder()
                    .creditorAccountId(String.valueOf(majorCreditorAccountId))
                    .build(),
                null
            );

        checkResponseForError(response, "getAtAGlance");

        GetMajorCreditorAccountAtAGlanceResponse mapped = atAGlanceResponseMapper.toOpal(response.responseEntity);
        mapped.setVersion(response.responseEntity.getMajorCreditor().getCreditorAccountVersion());

        return mapped;
    }

    @Override
    public GetMajorCreditorAccountHeaderSummaryResponse getHeaderSummary(Long majorCreditorAccountId) {
        Response<GetMajorCreditorAccountHeaderSummaryLegacyResponse> response =
            gatewayService.postToGateway(
                GET_MAJOR_CREDITOR_ACCOUNT_HEADER_SUMMARY,
                GetMajorCreditorAccountHeaderSummaryLegacyResponse.class,
                GetMajorCreditorAccountHeaderSummaryLegacyRequest.builder()
                    .creditorAccountId(String.valueOf(majorCreditorAccountId))
                    .build(),
                null
            );

        checkResponseForError(response, "getHeaderSummary");

        GetMajorCreditorAccountHeaderSummaryResponse mapped = headerSummaryResponseMapper.toOpal(
            response.responseEntity);

        MajorCreditorLegacy majorCreditor = response.responseEntity.getMajorCreditor();
        mapped.setVersion(BigInteger.valueOf(majorCreditor.getAccountVersion()));

        return mapped;
    }

    @Override
    public GetMajorCreditorHistoryResponse getHistory(
        Long majorCreditorAccountId,
        LocalDate dateFrom,
        LocalDate dateTo,
        List<String> itemTypes
    ) {
        Response<GetMajorCreditorAccountHistoryLegacyResponse> response =
            gatewayService.postToGateway(
                GET_MAJOR_CREDITOR_ACCOUNT_HISTORY,
                GetMajorCreditorAccountHistoryLegacyResponse.class,
                createGetMajorCreditorAccountHistoryRequest(majorCreditorAccountId, dateFrom, dateTo, itemTypes),
                null
            );

        checkResponseForError(response, "getHistory");

        return historyResponseMapper.toOpal(response.responseEntity);
    }

    static GetMajorCreditorAccountHistoryLegacyRequest createGetMajorCreditorAccountHistoryRequest(
        Long majorCreditorAccountId,
        LocalDate dateFrom,
        LocalDate dateTo,
        List<String> itemTypes
    ) {
        return GetMajorCreditorAccountHistoryLegacyRequest.builder()
            .creditorAccountId(String.valueOf(majorCreditorAccountId))
            .fromDate(dateFrom)
            .toDate(dateTo)
            .itemTypes(toLegacyHistoryItemTypes(itemTypes))
            .build();
    }

    private static List<String> toLegacyHistoryItemTypes(List<String> itemTypes) {
        List<String> legacyItemTypes = queryValues(itemTypes).stream()
            .map(HistoryItemType::fromValue)
            .map(HistoryItemType::getResponseValue)
            .toList();

        return legacyItemTypes.isEmpty() ? null : legacyItemTypes;
    }

    private static List<String> queryValues(List<String> itemTypes) {
        if (itemTypes == null) {
            return List.of();
        }

        return itemTypes.stream()
            .flatMap(rawValue -> rawValue == null ? Stream.of("") : Arrays.stream(rawValue.split(",", -1)))
            .map(String::trim)
            .filter(itemType -> !itemType.isEmpty())
            .toList();
    }

    private static <T> void checkResponseForError(Response<T> response, String method) {
        if (response.isError()) {
            log.error(":{}: Legacy Gateway response: HTTP Response Code {}", method, response.code);
            if (response.isException()) {
                log.error(":{}: Exception Message:", method, response.exception);
                throw createGatewayException(response.code, "Legacy gateway exception",
                    response.body, response.exception);
            } else if (response.isLegacyFailure()) {
                log.error(":{}: Legacy Failure: Body:\n{}", method, response.body);
                throw createGatewayException(response.code, "Legacy gateway returned failure", response.body, null);
            }
            throw createGatewayException(response.code, "Legacy gateway error", response.body, null);
        } else if (response.isSuccessful()) {
            log.info(":{}: Legacy Gateway response: Success.", method);
        }
    }

    private static RuntimeException createGatewayException(
        HttpStatusCode status,
        String fallbackStatusText,
        String responseBody,
        Throwable exception
    ) {
        HttpStatusCode responseStatus = status == null ? HttpStatus.INTERNAL_SERVER_ERROR : status;
        String statusText = exception != null && exception.getMessage() != null
            ? exception.getMessage()
            : fallbackStatusText;
        byte[] body = responseBody == null ? null : responseBody.getBytes(StandardCharsets.UTF_8);

        if (responseStatus.is4xxClientError()) {
            return HttpClientErrorException.create(
                responseStatus,
                statusText,
                HttpHeaders.EMPTY,
                body,
                StandardCharsets.UTF_8
            );
        }

        return HttpServerErrorException.create(
            responseStatus,
            statusText,
            HttpHeaders.EMPTY,
            body,
            StandardCharsets.UTF_8
        );
    }
}
