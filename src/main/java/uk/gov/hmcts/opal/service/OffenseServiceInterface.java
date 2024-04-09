package uk.gov.hmcts.opal.service;

import uk.gov.hmcts.opal.dto.search.OffenseSearchDto;
import uk.gov.hmcts.opal.entity.OffenseEntity;

import java.util.List;

public interface OffenseServiceInterface {

    OffenseEntity getOffense(long offenseId);

    List<OffenseEntity> searchOffenses(OffenseSearchDto criteria);
}
