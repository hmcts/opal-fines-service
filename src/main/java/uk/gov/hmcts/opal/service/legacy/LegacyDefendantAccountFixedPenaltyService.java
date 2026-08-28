package uk.gov.hmcts.opal.service.legacy;

import static uk.gov.hmcts.opal.dto.legacy.utils.ValidationUtils.checkResponseForError;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.common.legacy.service.GatewayService;
import uk.gov.hmcts.opal.dto.GetDefendantAccountFixedPenaltyResponse;
import uk.gov.hmcts.opal.dto.common.FixedPenaltyTicketDetails;
import uk.gov.hmcts.opal.dto.common.VehicleFixedPenaltyDetails;
import uk.gov.hmcts.opal.dto.legacy.LegacyDefendantAccountGetFixedPenaltyRequest;
import uk.gov.hmcts.opal.dto.legacy.LegacyDefendantAccountGetFixedPenaltyResponse;
import uk.gov.hmcts.opal.service.iface.DefendantAccountFixedPenaltyServiceInterface;
import uk.gov.hmcts.opal.common.legacy.service.GatewayService.Response;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "opal.LegacyDefendantAccountFixedPenaltyService")
public class LegacyDefendantAccountFixedPenaltyService implements DefendantAccountFixedPenaltyServiceInterface {


    public static final String GET_FIXED_PENALTY = "getDefendantAccountFixedPenalty";

    /* ---- Services ---- */
    private final GatewayService gatewayService;

    @Override
    public GetDefendantAccountFixedPenaltyResponse getDefendantAccountFixedPenalty(Long defendantAccountId) {
        log.debug(":getFixedPenalty: id: {}", defendantAccountId);

        try {
            Response<LegacyDefendantAccountGetFixedPenaltyResponse> response = gatewayService.postToGateway(
                GET_FIXED_PENALTY, LegacyDefendantAccountGetFixedPenaltyResponse.class,
                createLegacyDefendantAccountGetFixedPenaltyRequest(defendantAccountId.toString()),
                null);

            checkResponseForError(response, "getDefendantAccountFixedPenalty");

            return toAccountFixedPenaltyResponse(response.responseEntity);
        } catch (RuntimeException e) {
            log.error(":getDefendantAccountFixedPenalty: problem with call to Legacy: {}",
                e.getClass().getName());
            log.error(":getDefendantAccountFixedPenalty:", e);
            throw e;
        }
    }

    private GetDefendantAccountFixedPenaltyResponse toAccountFixedPenaltyResponse(
        LegacyDefendantAccountGetFixedPenaltyResponse responseEntity) {
        GetDefendantAccountFixedPenaltyResponse response = null;

        if (responseEntity != null) {
            response = GetDefendantAccountFixedPenaltyResponse
                .builder()
                .vehicleFixedPenaltyFlag(responseEntity.isVehicleFixedPenaltyFlag())
                .fixedPenaltyTicketDetails(responseEntity.isVehicleFixedPenaltyFlag()
                    ? buildEmptyFixedPenalty()
                    : FixedPenaltyTicketDetails
                        .builder()
                        .issuingAuthority(responseEntity.getFixedPenaltyDetails().getIssuingAuthority())
                        .ticketNumber(responseEntity.getFixedPenaltyDetails().getTicketNumber())
                        .timeOfOffence(responseEntity.getFixedPenaltyDetails().getTimeOfOffence())
                        .placeOfOffence(responseEntity.getFixedPenaltyDetails().getPlaceOfOffence())
                        .build())
                .vehicleFixedPenaltyDetails(responseEntity.isVehicleFixedPenaltyFlag()
                    ? VehicleFixedPenaltyDetails
                    .builder()
                    .vehicleRegistrationNumber(responseEntity.getVehicleFixedPenaltyDetails().getFpRegistrationNumber())
                    .vehicleDriversLicense(responseEntity.getVehicleFixedPenaltyDetails().getFpDrivingLicense())
                    .noticeNumber(responseEntity.getVehicleFixedPenaltyDetails().getNoticeToOwnerOrHirerNumber())
                    .dateNoticeIssued(responseEntity.getVehicleFixedPenaltyDetails().getDateNoticeIssued())
                    .build() : buildEmptyVehicleFixedPenalty())
                .build();
        }

        return response;
    }

    private @NotNull VehicleFixedPenaltyDetails buildEmptyVehicleFixedPenalty() {
        return VehicleFixedPenaltyDetails.builder().build();
    }

    private @NotNull FixedPenaltyTicketDetails buildEmptyFixedPenalty() {
        return FixedPenaltyTicketDetails.builder().build();
    }

    private Object createLegacyDefendantAccountGetFixedPenaltyRequest(String defendantAccountId) {
        return LegacyDefendantAccountGetFixedPenaltyRequest.builder()
            .defendantAccountId(defendantAccountId)
            .build();
    }

}
