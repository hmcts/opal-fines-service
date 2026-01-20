package uk.gov.hmcts.opal.service.opal;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.dto.GetDefendantAccountFixedPenaltyResponse;
import uk.gov.hmcts.opal.dto.common.FixedPenaltyTicketDetails;
import uk.gov.hmcts.opal.dto.common.VehicleFixedPenaltyDetails;
import uk.gov.hmcts.opal.entity.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.FixedPenaltyOffenceEntity;
import uk.gov.hmcts.opal.repository.DefendantAccountRepository;
import uk.gov.hmcts.opal.repository.FixedPenaltyOffenceRepository;
import uk.gov.hmcts.opal.service.iface.DefendantAccountFixedPenaltyServiceInterface;
import uk.gov.hmcts.opal.util.DateTimeUtils;

@Service
@Slf4j(topic = "opal.OpalDefendantAccountService")
@RequiredArgsConstructor
public class OpalDefendantAccountFixedPenaltyService implements DefendantAccountFixedPenaltyServiceInterface {

    private final DefendantAccountRepository defendantAccountRepository;

    private final FixedPenaltyOffenceRepository fixedPenaltyOffenceRepository;

    @Transactional(readOnly = true)
    public DefendantAccountEntity getDefendantAccountById(long defendantAccountId) {
        return defendantAccountRepository.findById(defendantAccountId)
            .orElseThrow(() -> new EntityNotFoundException(
                "Defendant Account not found with id: " + defendantAccountId));
    }

    @Override
    @Transactional(readOnly = true)
    public GetDefendantAccountFixedPenaltyResponse getDefendantAccountFixedPenalty(Long defendantAccountId) {
        log.debug(":getDefendantAccountFixedPenalty (Opal): id={}", defendantAccountId);

        DefendantAccountEntity account = getDefendantAccountById(defendantAccountId);

        FixedPenaltyOffenceEntity offence = fixedPenaltyOffenceRepository.findByDefendantAccountId(defendantAccountId)
            .orElseThrow(() -> new EntityNotFoundException(
                "Fixed Penalty Offence not found for account: " + defendantAccountId));

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
