package uk.gov.hmcts.opal.service;

import uk.gov.hmcts.opal.dto.search.ControlTotalSearchDto;
import uk.gov.hmcts.opal.entity.ControlTotalEntity;

import java.util.List;

public interface ControlTotalServiceInterface {

    ControlTotalEntity getControlTotal(long controlTotalId);

    List<ControlTotalEntity> searchControlTotals(ControlTotalSearchDto criteria);
}
