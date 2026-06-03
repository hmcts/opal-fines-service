package uk.gov.hmcts.opal.service.legacy;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
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
import uk.gov.hmcts.opal.dto.GetMajorCreditorAccountHeaderSummaryResponse;
import uk.gov.hmcts.opal.dto.legacy.GetMajorCreditorAccountHeaderSummaryLegacyRequest;
import uk.gov.hmcts.opal.dto.legacy.GetMajorCreditorAccountHeaderSummaryLegacyResponse;
import uk.gov.hmcts.opal.dto.legacy.GetMajorCreditorAccountHeaderSummaryLegacyResponse.MajorCreditorLegacy;
import uk.gov.hmcts.opal.mapper.legacy.GetMajorCreditorAccountHeaderSummaryResponseLegacyMapper;
import uk.gov.hmcts.opal.service.iface.MajorCreditorAccountServiceInterface;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "opal.LegacyMajorCreditorAccountService")
public class LegacyMajorCreditorAccountService implements MajorCreditorAccountServiceInterface {

    public static final String GET_MAJOR_CREDITOR_ACCOUNT_HEADER_SUMMARY =
        "LIBRA.get_major_creditor_account_header_summary";

    private final GatewayService gatewayService;
    private final GetMajorCreditorAccountHeaderSummaryResponseLegacyMapper headerSummaryResponseMapper;

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
