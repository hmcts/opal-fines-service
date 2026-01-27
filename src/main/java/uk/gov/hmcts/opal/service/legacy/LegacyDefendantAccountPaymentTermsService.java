package uk.gov.hmcts.opal.service.legacy;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.AddPaymentCardRequestResponse;
import uk.gov.hmcts.opal.dto.GetDefendantAccountPaymentTermsResponse;
import uk.gov.hmcts.opal.dto.PaymentTerms;
import uk.gov.hmcts.opal.dto.PostedDetails;
import uk.gov.hmcts.opal.dto.common.InstalmentPeriod;
import uk.gov.hmcts.opal.dto.common.PaymentTermsType;
import uk.gov.hmcts.opal.dto.legacy.AddPaymentCardLegacyRequest;
import uk.gov.hmcts.opal.dto.legacy.AddPaymentCardLegacyResponse;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetDefendantAccountPaymentTermsResponse;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetDefendantAccountRequest;
import uk.gov.hmcts.opal.dto.legacy.LegacyInstalmentPeriod;
import uk.gov.hmcts.opal.dto.legacy.LegacyPaymentTerms;
import uk.gov.hmcts.opal.dto.legacy.LegacyPaymentTermsType;
import uk.gov.hmcts.opal.dto.legacy.LegacyPostedDetails;
import uk.gov.hmcts.opal.service.iface.DefendantAccountPaymentTermsServiceInterface;
import uk.gov.hmcts.opal.service.legacy.GatewayService.Response;
import uk.gov.hmcts.opal.util.VersionUtils;

import java.math.BigInteger;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "opal.LegacyDefendantAccountPaymentTermsService")
public class LegacyDefendantAccountPaymentTermsService implements DefendantAccountPaymentTermsServiceInterface {

    public static final String GET_PAYMENT_TERMS = "LIBRA.get_payment_terms";
    public static final String ADD_PAYMENT_CARD_REQUEST = "LIBRA.of_add_defendant_account_pcr";

    private final GatewayService gatewayService;

    @Override
    public GetDefendantAccountPaymentTermsResponse getPaymentTerms(Long defendantAccountId) {

        Response<LegacyGetDefendantAccountPaymentTermsResponse> response = gatewayService.postToGateway(
            GET_PAYMENT_TERMS, LegacyGetDefendantAccountPaymentTermsResponse.class,
            createGetDefendantAccountRequest(defendantAccountId.toString()), null);

        if (response.isError()) {
            log.error(":getPaymentTerms: Legacy Gateway response: HTTP Response Code: {}", response.code);
            if (response.isException()) {
                log.error(":getPaymentTerms:", response.exception);
            } else if (response.isLegacyFailure()) {
                log.error(":getPaymentTerms: Legacy Gateway: body: \n{}", response.body);
                LegacyGetDefendantAccountPaymentTermsResponse responseEntity = response.responseEntity;
                log.error(":getPaymentTerms: Legacy Gateway: entity: \n{}", responseEntity.toXml());
            }
        } else if (response.isSuccessful()) {
            log.info(":getPaymentTerms: Legacy Gateway response: Success.");
        }

        return toPaymentTermsResponse(response.responseEntity);
    }

    /* This is probably common code that will be needed across multiple Legacy requests to get
    Defendant Account details. */
    public static LegacyGetDefendantAccountRequest createGetDefendantAccountRequest(String defendantAccountId) {
        return LegacyGetDefendantAccountRequest.builder()
            .defendantAccountId(defendantAccountId)
            .build();
    }

    private GetDefendantAccountPaymentTermsResponse toPaymentTermsResponse(
        LegacyGetDefendantAccountPaymentTermsResponse legacy) {

        if (legacy == null) {
            return null;
        }

        return GetDefendantAccountPaymentTermsResponse.builder()
            .version(Optional.ofNullable(legacy.getVersion())
                .map(v -> BigInteger.valueOf(v.longValue())).orElse(BigInteger.ONE))
            .paymentTerms(toPaymentTerms(legacy.getPaymentTerms()))
            .paymentCardLastRequested(legacy.getPaymentCardLastRequested())
            .lastEnforcement(legacy.getLastEnforcement())
            .build();
    }

    private static PaymentTerms toPaymentTerms(LegacyPaymentTerms legacy) {
        if (legacy == null) {
            return null;
        }
        return PaymentTerms.builder()
            .daysInDefault(legacy.getDaysInDefault())
            .dateDaysInDefaultImposed(legacy.getDateDaysInDefaultImposed())
            .extension(legacy.isExtension())
            .reasonForExtension(legacy.getReasonForExtension())
            .paymentTermsType(toPaymentTermsType(legacy.getPaymentTermsType()))
            .effectiveDate(legacy.getEffectiveDate())
            .instalmentPeriod(toInstalmentPeriod(legacy.getInstalmentPeriod()))
            .lumpSumAmount(legacy.getLumpSumAmount())
            .instalmentAmount(legacy.getInstalmentAmount())
            .postedDetails(toPostedDetails(legacy.getPostedDetails()))
            .build();
    }

    private static PaymentTermsType toPaymentTermsType(LegacyPaymentTermsType legacy) {
        if (legacy == null) {
            return null;
        }

        PaymentTermsType.PaymentTermsTypeCode code = null;
        if (legacy.getPaymentTermsTypeCode() != null) {
            code = PaymentTermsType.PaymentTermsTypeCode.fromValue(
                legacy.getPaymentTermsTypeCode().name()
            );
        }

        return PaymentTermsType.builder()
            .paymentTermsTypeCode(code)
            .build();
    }

    private static InstalmentPeriod toInstalmentPeriod(LegacyInstalmentPeriod legacy) {
        if (legacy == null) {
            return null;
        }

        InstalmentPeriod.InstalmentPeriodCode code = null;
        if (legacy.getInstalmentPeriodCode() != null) {
            code = InstalmentPeriod.InstalmentPeriodCode.fromValue(
                legacy.getInstalmentPeriodCode().name()
            );
        }

        return InstalmentPeriod.builder()
            .instalmentPeriodCode(code)
            .build();
    }

    private static PostedDetails toPostedDetails(LegacyPostedDetails legacy) {
        if (legacy == null) {
            return null;
        }

        return PostedDetails.builder()
            .postedDate(legacy.getPostedDate())
            .postedBy(legacy.getPostedBy())
            .postedByName(legacy.getPostedByName())
            .build();
    }

    @Override
    public AddPaymentCardRequestResponse addPaymentCardRequest(
        Long defendantAccountId,
        String businessUnitId,
        String businessUnitUserId,
        String ifMatch,
        String authHeader
    ) {
        log.info(":addPaymentCardRequest (Legacy): accountId={}, bu={}", defendantAccountId, businessUnitId);

        BigInteger version = VersionUtils.extractBigInteger(ifMatch);
        AddPaymentCardLegacyRequest request = buildLegacyRequest(defendantAccountId, businessUnitId,
            businessUnitUserId, version.toString());

        AddPaymentCardLegacyResponse response = callGateway(request);
        Long id = Long.valueOf(response.getDefendantAccountId());

        return new AddPaymentCardRequestResponse(id);
    }

    private AddPaymentCardLegacyRequest buildLegacyRequest(
        Long defendantAccountId,
        String businessUnitId,
        String businessUnitUserId,
        String version
    ) {
        return AddPaymentCardLegacyRequest.builder()
            .defendantAccountId(String.valueOf(defendantAccountId))
            .businessUnitId(businessUnitId)
            .businessUnitUserId(businessUnitUserId)
            .version(version)
            .build();
    }

    private AddPaymentCardLegacyResponse callGateway(AddPaymentCardLegacyRequest request) {

        Response<AddPaymentCardLegacyResponse> gw =
            gatewayService.postToGateway(
                ADD_PAYMENT_CARD_REQUEST,
                AddPaymentCardLegacyResponse.class,
                request,
                null
            );

        if (gw.isError()) {
            handleGatewayError(gw);
        }

        if (gw.responseEntity == null) {
            throw new IllegalArgumentException("Legacy response missing");
        }

        return gw.responseEntity;
    }

    private void handleGatewayError(Response<?> gw) {

        log.error(":addPaymentCardRequest: Legacy Gateway error {}", gw.code);

        if (gw.isException()) {
            log.error(":addPaymentCardRequest: exception", gw.exception);
            throw new IllegalArgumentException("Legacy gateway exception", gw.exception);
        }

        if (gw.isLegacyFailure()) {
            log.error(":addPaymentCardRequest: legacy failure:\n{}", gw.body);
            throw new IllegalArgumentException("Legacy gateway returned failure");
        }

        throw new IllegalArgumentException("Legacy gateway error: " + gw.code);
    }
}
