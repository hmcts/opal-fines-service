package uk.gov.hmcts.opal.service.opal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.dto.GetDefendantAccountFixedPenaltyResponse;
import uk.gov.hmcts.opal.dto.common.FixedPenaltyTicketDetails;
import uk.gov.hmcts.opal.dto.common.VehicleFixedPenaltyDetails;
import uk.gov.hmcts.opal.entity.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.FixedPenaltyOffenceEntity;
import uk.gov.hmcts.opal.service.iface.DefendantAccountFixedPenaltyServiceInterface;
import uk.gov.hmcts.opal.service.persistence.DefendantAccountRepositoryService;
import uk.gov.hmcts.opal.service.persistence.FixedPenaltyOffenceRepositoryService;
import uk.gov.hmcts.opal.util.DateTimeUtils;

@Service
@Slf4j(topic = "opal.OpalDefendantAccountService")
@RequiredArgsConstructor
public class OpalDefendantAccountFixedPenaltyService implements DefendantAccountFixedPenaltyServiceInterface {

    private final DefendantAccountRepositoryService defendantAccountRepositoryService;

    private final FixedPenaltyOffenceRepositoryService fixedPenaltyOffenceRepositoryService;

    @Override
    @Transactional(readOnly = true)
    public GetDefendantAccountFixedPenaltyResponse getDefendantAccountFixedPenalty(Long defendantAccountId) {
        log.debug(":getDefendantAccountFixedPenalty (Opal): id={}", defendantAccountId);

        DefendantAccountEntity account = defendantAccountRepositoryService.findById(defendantAccountId);

        FixedPenaltyOffenceEntity offence = fixedPenaltyOffenceRepositoryService
            .findByDefendantAccountId(defendantAccountId);

        return toFixedPenaltyResponse(account, offence);
    }

    private static GetDefendantAccountFixedPenaltyResponse toFixedPenaltyResponse(
        DefendantAccountEntity account, FixedPenaltyOffenceEntity offence) {

        boolean isVehicle =
            offence.getVehicleRegistration() != null
                && !"NV".equalsIgnoreCase(offence.getVehicleRegistration());

        FixedPenaltyTicketDetails ticketDetails = FixedPenaltyTicketDetails.builder()
            .issuingAuthority(account.getOriginatorName())
            .ticketNumber(offence.getTicketNumber())
            .timeOfOffence(
                offence.getTimeOfOffence() != null
                    ? offence.getTimeOfOffence().toString()
                    : null
            )
            .placeOfOffence(offence.getOffenceLocation())
            .build();

        VehicleFixedPenaltyDetails vehicleDetails = isVehicle
            ? VehicleFixedPenaltyDetails.builder()
            .vehicleRegistrationNumber(offence.getVehicleRegistration())
            .vehicleDriversLicense(offence.getLicenceNumber())
            .noticeNumber(offence.getNoticeNumber())
            .dateNoticeIssued(DateTimeUtils.toString(offence.getIssuedDate()))
            .build()
            : null;
        return GetDefendantAccountFixedPenaltyResponse.builder()
            .vehicleFixedPenaltyFlag(isVehicle)
            .fixedPenaltyTicketDetails(ticketDetails)
            .vehicleFixedPenaltyDetails(vehicleDetails)
            .version(account.getVersion())
            .build();
    }

}
