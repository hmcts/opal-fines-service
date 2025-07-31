package uk.gov.hmcts.opal.disco;

import uk.gov.hmcts.opal.dto.search.FixedPenaltyOffenceSearchDto;
import uk.gov.hmcts.opal.entity.FixedPenaltyOffenceEntity;

import java.util.List;

public interface FixedPenaltyOffenceServiceInterface {

    FixedPenaltyOffenceEntity getFixedPenaltyOffence(long fixedPenaltyOffenceId);

    List<FixedPenaltyOffenceEntity> searchFixedPenaltyOffences(FixedPenaltyOffenceSearchDto criteria);
}
