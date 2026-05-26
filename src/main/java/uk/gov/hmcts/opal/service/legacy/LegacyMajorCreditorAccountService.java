package uk.gov.hmcts.opal.service.legacy;

import java.math.BigInteger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.common.legacy.service.GatewayService;
import uk.gov.hmcts.opal.common.legacy.service.GatewayService.Response;
import uk.gov.hmcts.opal.dto.GetMajorCreditorAccountHeaderSummaryResponse;
import uk.gov.hmcts.opal.dto.legacy.GetMajorCreditorAccountHeaderSummaryLegacyRequest;
import uk.gov.hmcts.opal.dto.legacy.GetMajorCreditorAccountHeaderSummaryLegacyResponse;
import uk.gov.hmcts.opal.dto.legacy.GetMajorCreditorAccountHeaderSummaryLegacyResponse.MajorCreditorLegacy;
import uk.gov.hmcts.opal.mapper.legacy.GetMajorCreditorAccountHeaderSummaryResponseLegacyMapper;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "opal.LegacyMajorCreditorAccountService")
public class LegacyMajorCreditorAccountService {

    public static final String GET_MAJOR_CREDITOR_ACCOUNT_HEADER_SUMMARY =
        "LIBRA.get_major_creditor_account_header_summary";

    private final GatewayService gatewayService;
    private final GetMajorCreditorAccountHeaderSummaryResponseLegacyMapper headerSummaryResponseMapper;

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
            } else if (response.isLegacyFailure()) {
                log.error(":{}: Legacy Failure: Body:\n{}", method, response.body);
            }
        } else if (response.isSuccessful()) {
            log.info(":{}: Legacy Gateway response: Success.", method);
        }
    }
}
