package uk.gov.hmcts.opal.service;

import uk.gov.hmcts.opal.dto.search.EnforcementSearchDto;
import uk.gov.hmcts.opal.entity.EnforcementEntity;

import java.util.List;

public interface EnforcementServiceInterface {

    EnforcementEntity getEnforcement(long enforcementId);

    List<EnforcementEntity> searchEnforcements(EnforcementSearchDto criteria);
}
