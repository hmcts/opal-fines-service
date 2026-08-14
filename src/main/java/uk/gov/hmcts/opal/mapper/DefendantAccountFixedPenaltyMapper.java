package uk.gov.hmcts.opal.mapper;

import org.mapstruct.Mapper;
import uk.gov.hmcts.opal.entity.FixedPenaltyOffenceEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.generated.model.FixedPenaltyTicketDetailsCommon;
import uk.gov.hmcts.opal.generated.model.GetDefendantAccountFixedPenaltyResponse;
import uk.gov.hmcts.opal.generated.model.VehicleFixedPenaltyDetailsCommon;

@Mapper(componentModel = "spring")
public interface DefendantAccountFixedPenaltyMapper {

    default GetDefendantAccountFixedPenaltyResponse toResponse(DefendantAccountEntity account,
        FixedPenaltyOffenceEntity offence) {
        boolean isVehicle = isVehicleFixedPenalty(offence);

        return GetDefendantAccountFixedPenaltyResponse.builder()
            .vehicleFixedPenaltyFlag(isVehicle)
            .fixedPenaltyTicketDetails(toFixedPenaltyTicketDetails(account, offence))
            .vehicleFixedPenaltyDetails(isVehicle ? toVehicleFixedPenaltyDetails(offence) : null)
            .version(account.getVersion())
            .build();
    }

    private static boolean isVehicleFixedPenalty(FixedPenaltyOffenceEntity offence) {
        return offence.getVehicleRegistration() != null
            && !"NV".equalsIgnoreCase(offence.getVehicleRegistration());
    }

    private static FixedPenaltyTicketDetailsCommon toFixedPenaltyTicketDetails(DefendantAccountEntity account,
        FixedPenaltyOffenceEntity offence) {
        return FixedPenaltyTicketDetailsCommon.builder()
            .issuingAuthority(account.getOriginatorName())
            .ticketNumber(offence.getTicketNumber())
            .timeOfOffence(offence.getTimeOfOffence() == null ? null : offence.getTimeOfOffence().toString())
            .placeOfOffence(offence.getOffenceLocation())
            .build();
    }

    private static VehicleFixedPenaltyDetailsCommon toVehicleFixedPenaltyDetails(FixedPenaltyOffenceEntity offence) {
        return VehicleFixedPenaltyDetailsCommon.builder()
            .vehicleRegistrationNumber(offence.getVehicleRegistration())
            .vehicleDriversLicense(offence.getLicenceNumber())
            .noticeNumber(offence.getNoticeNumber())
            .dateNoticeIssued(offence.getIssuedDate())
            .build();
    }
}
