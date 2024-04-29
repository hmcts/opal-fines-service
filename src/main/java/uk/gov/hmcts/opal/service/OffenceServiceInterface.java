package uk.gov.hmcts.opal.service;

import uk.gov.hmcts.opal.dto.search.OffenceSearchDto;
import uk.gov.hmcts.opal.entity.OffenceEntity;

import java.util.List;

public interface OffenceServiceInterface {

    OffenceEntity getOffence(long offenceId);

    List<OffenceEntity> searchOffences(OffenceSearchDto criteria);
}
