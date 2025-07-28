package uk.gov.hmcts.opal.disco;

import uk.gov.hmcts.opal.dto.search.CourtFeeSearchDto;
import uk.gov.hmcts.opal.entity.CourtFeeEntity;

import java.util.List;

public interface CourtFeeServiceInterface {

    CourtFeeEntity getCourtFee(long courtFeeId);

    List<CourtFeeEntity> searchCourtFees(CourtFeeSearchDto criteria);
}
