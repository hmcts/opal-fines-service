package uk.gov.hmcts.opal.disco;

import uk.gov.hmcts.opal.dto.search.PrisonSearchDto;
import uk.gov.hmcts.opal.entity.PrisonEntity;

import java.util.List;

public interface PrisonServiceInterface {

    PrisonEntity getPrison(long prisonId);

    List<PrisonEntity> searchPrisons(PrisonSearchDto criteria);
}
