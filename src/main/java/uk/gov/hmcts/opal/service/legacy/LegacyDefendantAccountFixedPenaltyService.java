package uk.gov.hmcts.opal.service.legacy;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.common.legacy.service.GatewayService;
import uk.gov.hmcts.opal.dto.legacy.FixedPenaltyTicketDetails;
import uk.gov.hmcts.opal.dto.legacy.LegacyDefendantAccountGetFixedPenaltyRequest;
import uk.gov.hmcts.opal.dto.legacy.LegacyDefendantAccountGetFixedPenaltyResponse;
import uk.gov.hmcts.opal.dto.legacy.VehicleFixedPenaltyDetails;
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

    private final GatewayService gatewayService;

    @Override
    public GetDefendantAccountFixedPenaltyResponse getDefendantAccountFixedPenalty(Long defendantAccountId) {
        log.debug(":getDefendantAccountFixedPenalty: id: {}", defendantAccountId);

        try {
            Response<LegacyDefendantAccountGetFixedPenaltyResponse> response = gatewayService.postToGateway(
                GET_FIXED_PENALTY, LegacyDefendantAccountGetFixedPenaltyResponse.class,
                createLegacyDefendantAccountGetFixedPenaltyRequest(defendantAccountId),
                null);

            return toAccountFixedPenaltyResponse(response.responseEntity);
        } catch (RuntimeException e) {
            log.error(":getDefendantAccountFixedPenalty: problem with call to Legacy: {}",
                e.getClass().getName());
            log.error(":getDefendantAccountFixedPenalty:", e);
            throw e;
        }
    }

    private GetDefendantAccountFixedPenaltyResponse toAccountFixedPenaltyResponse(
        LegacyDefendantAccountGetFixedPenaltyResponse entity) {
        if (entity == null) {
            return null;
        }
        return GetDefendantAccountFixedPenaltyResponse.builder()
            .vehicleFixedPenaltyFlag(entity.isVehicleFixedPenaltyFlag())
            .fixedPenaltyTicketDetails(buildFixedPenaltyTicketDetails(entity.getFixedPenaltyDetails()))
            .vehicleFixedPenaltyDetails(buildVehicleFixedPenaltyDetails(entity.getVehicleFixedPenaltyDetails()))
            .version(entity.getVersion())
            .build();
    }

    private FixedPenaltyTicketDetailsCommonStrict buildFixedPenaltyTicketDetails(FixedPenaltyTicketDetails details) {
        if (details == null) {
            return null;
        }
        return FixedPenaltyTicketDetailsCommonStrict.builder()
            .issuingAuthority(details.getIssuingAuthority())
            .ticketNumber(details.getTicketNumber())
            .timeOfOffence(details.getTimeOfOffence())
            .placeOfOffence(details.getPlaceOfOffence())
            .build();
    }

    private VehicleFixedPenaltyDetailsCommonStrict buildVehicleFixedPenaltyDetails(VehicleFixedPenaltyDetails details) {
        if (details == null) {
            return null;
        }
        return VehicleFixedPenaltyDetailsCommonStrict.builder()
            .vehicleRegistrationNumber(details.getFpRegistrationNumber())
            .vehicleDriversLicense(details.getFpDrivingLicense())
            .noticeNumber(details.getNoticeToOwnerOrHirerNumber())
            .dateNoticeIssued(LocalDate.parse(details.getDateNoticeIssued()))
            .build();
    }

    private LegacyDefendantAccountGetFixedPenaltyRequest
        createLegacyDefendantAccountGetFixedPenaltyRequest(Long defendantAccountId) {
        return LegacyDefendantAccountGetFixedPenaltyRequest.builder()
            .defendantAccountId(defendantAccountId)
            .build();
    }

}
