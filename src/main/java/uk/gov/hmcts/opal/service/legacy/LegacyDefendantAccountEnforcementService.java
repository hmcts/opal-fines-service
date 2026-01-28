package uk.gov.hmcts.opal.service.legacy;

import static uk.gov.hmcts.opal.service.legacy.LegacyDefendantAccountBuilders.toEnforcementStatusResponse;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.AddDefendantAccountEnforcementRequest;
import uk.gov.hmcts.opal.dto.AddEnforcementResponse;
import uk.gov.hmcts.opal.dto.EnforcementStatus;
import uk.gov.hmcts.opal.dto.PaymentTerms;
import uk.gov.hmcts.opal.dto.PostedDetails;
import uk.gov.hmcts.opal.dto.ResultResponse;
import uk.gov.hmcts.opal.dto.common.InstalmentPeriod;
import uk.gov.hmcts.opal.dto.common.PaymentTermsType;
import uk.gov.hmcts.opal.dto.legacy.AddDefendantAccountEnforcementLegacyRequest;
import uk.gov.hmcts.opal.dto.legacy.AddDefendantAccountEnforcementLegacyResponse;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetDefendantAccountEnforcementStatusResponse;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetDefendantAccountRequest;
import uk.gov.hmcts.opal.dto.legacy.LegacyInstalmentPeriod;
import uk.gov.hmcts.opal.dto.legacy.LegacyPaymentTerms;
import uk.gov.hmcts.opal.dto.legacy.LegacyPaymentTermsType;
import uk.gov.hmcts.opal.dto.legacy.LegacyPostedDetails;
import uk.gov.hmcts.opal.dto.legacy.ResultResponsesLegacy;
import uk.gov.hmcts.opal.dto.legacy.common.CourtReference;
import uk.gov.hmcts.opal.service.iface.DefendantAccountEnforcementServiceInterface;
import uk.gov.hmcts.opal.service.legacy.GatewayService.Response;
import uk.gov.hmcts.opal.service.opal.CourtService;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "opal.LegacyDefendantAccountEnforcementService")
public class LegacyDefendantAccountEnforcementService implements DefendantAccountEnforcementServiceInterface {

    public static final String ADD_ENFORCEMENT = "LIBRA.addEnforcement";
    public static final String GET_ENFORCEMENT_STATUS = "LIBRA.of_get_defendant_account_enf_status";

    private final GatewayService gatewayService;
    private final CourtService courtService;

    /* This is probably common code that will be needed across multiple Legacy requests to get
    Defendant Account details. */
    public static LegacyGetDefendantAccountRequest createGetDefendantAccountRequest(String defendantAccountId) {
        return LegacyGetDefendantAccountRequest.builder()
            .defendantAccountId(defendantAccountId)
            .build();
    }

    @Override
    public AddEnforcementResponse addEnforcement(Long defendantAccountId, String businessUnitId,
        String businessUnitUserId, String ifMatch, String authHeader, AddDefendantAccountEnforcementRequest request) {

        String cleanVersion = ifMatch.replace("\"", "");

        // build legacy request object
        AddDefendantAccountEnforcementLegacyRequest legacyRequest =
            AddDefendantAccountEnforcementLegacyRequest.builder()
                .defendantAccountId(String.valueOf(defendantAccountId))
                .businessUnitId(businessUnitId)
                .businessUnitUserId(businessUnitUserId)
                .version(Integer.parseInt(cleanVersion))
                .resultId(request != null && request.getResultId() != null ? request.getResultId().value() : null)
                .enforcementResultResponses(
                    mapResultResponses(request != null ? request.getEnforcementResultResponses() : null))
                .paymentTerms(mapPaymentTerms(request != null ? request.getPaymentTerms() : null))
                .build();

        Response<AddDefendantAccountEnforcementLegacyResponse> response = gatewayService.postToGateway(
            ADD_ENFORCEMENT, AddDefendantAccountEnforcementLegacyResponse.class,
            legacyRequest, null);

        if (response.isError()) {
            log.error(":AddEnforcement: Legacy error HTTP {}", response.code);
            if (response.isException()) {
                log.error(":AddEnforcement: exception:", response.exception);
            } else if (response.isLegacyFailure()) {
                log.error(":AddEnforcement: legacy failure body:\n{}", response.body);
            }
        } else if (response.isSuccessful()) {
            log.info(":AddEnforcement: Legacy success.");
        }

        AddDefendantAccountEnforcementLegacyResponse enforcementResponse = response.responseEntity;

        return AddEnforcementResponse.builder().enforcementId(enforcementResponse.getEnforcementId())
            .defendantAccountId(enforcementResponse.getDefendantAccountId()).version(enforcementResponse.getVersion())
            .build();

    }

    private List<ResultResponsesLegacy> mapResultResponses(List<ResultResponse> responses) {
        if (responses == null || responses.isEmpty()) {
            return Collections.emptyList();
        }
        return responses.stream()
            .filter(Objects::nonNull)
            .map(r -> ResultResponsesLegacy.builder()
                .parameterName(r.getParameterName())
                .response(r.getResponse())
                .build())
            .collect(Collectors.toList());
    }

    private LegacyPaymentTerms mapPaymentTerms(PaymentTerms pt) {
        if (pt == null) {
            return null;
        }

        return LegacyPaymentTerms.builder()
            .daysInDefault(pt.getDaysInDefault())
            .dateDaysInDefaultImposed(pt.getDateDaysInDefaultImposed())
            .extension(pt.isExtension())
            .reasonForExtension(pt.getReasonForExtension())
            .paymentTermsType(mapLegacyPaymentTermsType(pt.getPaymentTermsType()))
            .effectiveDate(pt.getEffectiveDate())
            .instalmentPeriod(mapLegacyInstalmentPeriod(pt.getInstalmentPeriod()))
            .lumpSumAmount(pt.getLumpSumAmount())
            .instalmentAmount(pt.getInstalmentAmount())
            .postedDetails(mapLegacyPostedDetails(pt.getPostedDetails()))
            .build();
    }

    private LegacyPostedDetails mapLegacyPostedDetails(PostedDetails pd) {
        if (pd == null) {
            return null;
        }
        LegacyPostedDetails lpd = new LegacyPostedDetails();
        lpd.setPostedDate(pd.getPostedDate());
        lpd.setPostedBy(pd.getPostedBy());
        lpd.setPostedByName(pd.getPostedByName());
        return lpd;
    }

    private LegacyPaymentTermsType mapLegacyPaymentTermsType(PaymentTermsType modern) {
        if (modern == null || modern.getPaymentTermsTypeCode() == null) {
            return null;
        }
        String code = modern.getPaymentTermsTypeCode().name();
        LegacyPaymentTermsType lpt = new LegacyPaymentTermsType();
        lpt.setPaymentTermsTypeCode(mapPaymentTermsTypeCodeEnum(code));
        return lpt;
    }

    private LegacyInstalmentPeriod mapLegacyInstalmentPeriod(InstalmentPeriod modern) {
        if (modern == null || modern.getInstalmentPeriodCode() == null) {
            return null;
        }
        String code = modern.getInstalmentPeriodCode().name();
        LegacyInstalmentPeriod lip = new LegacyInstalmentPeriod();
        lip.setInstalmentPeriodCode(mapInstalmentPeriodCodeEnum(code));
        return lip;
    }

    private LegacyPaymentTermsType.PaymentTermsTypeCode mapPaymentTermsTypeCodeEnum(String code) {
        if (code == null) {
            return null;
        }
        return switch (code.toUpperCase()) {
            case "B" -> LegacyPaymentTermsType.PaymentTermsTypeCode.B;
            case "P" -> LegacyPaymentTermsType.PaymentTermsTypeCode.P;
            case "I" -> LegacyPaymentTermsType.PaymentTermsTypeCode.I;
            default -> throw new IllegalArgumentException("Unknown PaymentTermsType code: " + code);
        };
    }

    private LegacyInstalmentPeriod.InstalmentPeriodCode mapInstalmentPeriodCodeEnum(String code) {
        if (code == null) {
            return null;
        }
        return switch (code.toUpperCase()) {
            case "W" -> LegacyInstalmentPeriod.InstalmentPeriodCode.W;
            case "M" -> LegacyInstalmentPeriod.InstalmentPeriodCode.M;
            case "F" -> LegacyInstalmentPeriod.InstalmentPeriodCode.F;
            default -> throw new IllegalArgumentException("Unknown InstalmentPeriod code: " + code);
        };
    }


    @Override
    public EnforcementStatus getEnforcementStatus(Long defendantAccountId) {
        log.debug(":getEnforcementStatus: id: {}", defendantAccountId);

        try {

            Response<LegacyGetDefendantAccountEnforcementStatusResponse> response = gatewayService.postToGateway(
                GET_ENFORCEMENT_STATUS, LegacyGetDefendantAccountEnforcementStatusResponse.class,
                createGetDefendantAccountRequest(defendantAccountId.toString()), null);

            if (response.isError()) {
                log.error(":getEnforcementStatus: Legacy Gateway response: HTTP Response Code: {}", response.code);
                if (response.isException()) {
                    log.error(":getEnforcementStatus:", response.exception);
                } else if (response.isLegacyFailure()) {
                    log.error(":getEnforcementStatus: Legacy Gateway: body: \n{}", response.body);
                    LegacyGetDefendantAccountEnforcementStatusResponse responseEntity = response.responseEntity;
                    log.error(":getEnforcementStatus: Legacy Gateway: entity: \n{}", responseEntity.toXml());
                }
            } else if (response.isSuccessful()) {
                log.info(":getEnforcementStatus: Legacy Gateway response: Success.");
            }

            LegacyGetDefendantAccountEnforcementStatusResponse enforcementStatus = response.responseEntity;
            populateCourtCode(enforcementStatus);
            return toEnforcementStatusResponse(enforcementStatus);

        } catch (RuntimeException e) {
            log.error(":getEnforcementStatus: problem with call to Legacy: {}", e.getClass().getName());
            log.error(":getEnforcementStatus:", e);
            throw e;
        }
    }

    private void populateCourtCode(LegacyGetDefendantAccountEnforcementStatusResponse enforcementStatus) {
        Optional.ofNullable(enforcementStatus)
            .map(es -> es.getEnforcementOverview())
            .map(eo -> eo.getEnforcementCourt()).ifPresent(this::populateCourtCode);

    }

    private void populateCourtCode(CourtReference courtRef) {
        courtRef.setCourtCode(courtService.getCourtById(courtRef.getCourtId()).getCourtCode());
    }

}
