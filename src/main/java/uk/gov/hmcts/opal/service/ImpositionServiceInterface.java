package uk.gov.hmcts.opal.service;

import uk.gov.hmcts.opal.dto.search.ImpositionSearchDto;
import uk.gov.hmcts.opal.entity.ImpositionEntity;

import java.util.List;

public interface ImpositionServiceInterface {

    ImpositionEntity getImposition(long impositionId);

    List<ImpositionEntity> searchImpositions(ImpositionSearchDto criteria);
}
