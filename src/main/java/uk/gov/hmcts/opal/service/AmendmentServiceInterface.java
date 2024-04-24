package uk.gov.hmcts.opal.service;

import uk.gov.hmcts.opal.dto.search.AmendmentSearchDto;
import uk.gov.hmcts.opal.entity.AmendmentEntity;

import java.util.List;

public interface AmendmentServiceInterface {

    AmendmentEntity getAmendment(long amendmentId);

    List<AmendmentEntity> searchAmendments(AmendmentSearchDto criteria);
}
