package uk.gov.hmcts.opal.service.legacy;

import static uk.gov.hmcts.opal.dto.legacy.utils.ValidationUtils.checkResponseForError;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.common.legacy.service.GatewayService;
import uk.gov.hmcts.opal.dto.legacy.LegacyDefendantAccountGetFixedPenaltyRequest;
import uk.gov.hmcts.opal.dto.legacy.LegacyDefendantAccountGetFixedPenaltyResponse;
import uk.gov.hmcts.opal.generated.model.FixedPenaltyTicketDetailsCommonStrict;
import uk.gov.hmcts.opal.generated.model.GetDefendantAccountFixedPenaltyResponse;
import uk.gov.hmcts.opal.generated.model.VehicleFixedPenaltyDetailsCommonStrict;
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
                    : FixedPenaltyTicketDetailsCommonStrict
                        .builder()
                        .issuingAuthority(responseEntity.getFixedPenaltyDetails().getIssuingAuthority())
                        .ticketNumber(responseEntity.getFixedPenaltyDetails().getTicketNumber())
                        .timeOfOffence(responseEntity.getFixedPenaltyDetails().getTimeOfOffence())
                        .placeOfOffence(responseEntity.getFixedPenaltyDetails().getPlaceOfOffence())
                        .build())
                .vehicleFixedPenaltyDetails(responseEntity.isVehicleFixedPenaltyFlag()
                    ? VehicleFixedPenaltyDetailsCommonStrict
                    .builder()
                    .vehicleRegistrationNumber(responseEntity.getVehicleFixedPenaltyDetails().getFpRegistrationNumber())
                    .vehicleDriversLicense(responseEntity.getVehicleFixedPenaltyDetails().getFpDrivingLicense())
                    .noticeNumber(responseEntity.getVehicleFixedPenaltyDetails().getNoticeToOwnerOrHirerNumber())
                    .dateNoticeIssued(
                        LocalDate.parse(responseEntity.getVehicleFixedPenaltyDetails().getDateNoticeIssued()))
                    .build() : buildEmptyVehicleFixedPenalty())
                .build();
        }

        return response;
    }

    private @NotNull VehicleFixedPenaltyDetailsCommonStrict buildEmptyVehicleFixedPenalty() {
        return VehicleFixedPenaltyDetailsCommonStrict
            .builder()
            .vehicleRegistrationNumber(JsonNullable.of(null))
            .vehicleDriversLicense(JsonNullable.of(null))
            .noticeNumber(JsonNullable.of(null))
            .dateNoticeIssued(JsonNullable.of(null))
            .build();
    }

    private @NotNull FixedPenaltyTicketDetailsCommonStrict buildEmptyFixedPenalty() {
        return FixedPenaltyTicketDetailsCommonStrict
            .builder()
            .issuingAuthority(JsonNullable.of(null))
            .ticketNumber(JsonNullable.of(null))
            .timeOfOffence(JsonNullable.of(null))
            .placeOfOffence(JsonNullable.of(null))
            .build();
    }

    private Object createLegacyDefendantAccountGetFixedPenaltyRequest(String defendantAccountId) {
        return LegacyDefendantAccountGetFixedPenaltyRequest.builder()
            .defendantAccountId(defendantAccountId)
            .build();
    }

}
