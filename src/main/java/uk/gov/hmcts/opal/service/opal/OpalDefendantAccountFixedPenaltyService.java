package uk.gov.hmcts.opal.service.opal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.FixedPenaltyOffenceEntity;
import uk.gov.hmcts.opal.generated.model.GetDefendantAccountFixedPenaltyResponse;
import uk.gov.hmcts.opal.mapper.DefendantAccountFixedPenaltyMapper;
import uk.gov.hmcts.opal.service.iface.DefendantAccountFixedPenaltyServiceInterface;
import uk.gov.hmcts.opal.service.persistence.DefendantAccountRepositoryService;
import uk.gov.hmcts.opal.service.persistence.FixedPenaltyOffenceRepositoryService;

@Service
@Slf4j(topic = "opal.OpalDefendantAccountService")
@RequiredArgsConstructor
public class OpalDefendantAccountFixedPenaltyService implements DefendantAccountFixedPenaltyServiceInterface {

    private final DefendantAccountRepositoryService defendantAccountRepositoryService;

    private final FixedPenaltyOffenceRepositoryService fixedPenaltyOffenceRepositoryService;

    private final DefendantAccountFixedPenaltyMapper defendantAccountFixedPenaltyMapper;

    @Override
    @Transactional(readOnly = true)
    public GetDefendantAccountFixedPenaltyResponse getDefendantAccountFixedPenalty(Long defendantAccountId) {
        log.debug(":getDefendantAccountFixedPenalty (Opal): id={}", defendantAccountId);

        DefendantAccountEntity account = defendantAccountRepositoryService.findById(defendantAccountId);

        FixedPenaltyOffenceEntity offence = fixedPenaltyOffenceRepositoryService
            .findByDefendantAccountId(defendantAccountId);

        return defendantAccountFixedPenaltyMapper.toResponse(account, offence);
    }

}
