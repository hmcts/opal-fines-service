package uk.gov.hmcts.opal.service;

import uk.gov.hmcts.opal.dto.search.EnforcerSearchDto;
import uk.gov.hmcts.opal.entity.EnforcerEntity;

import java.util.List;

public interface EnforcerServiceInterface {

    EnforcerEntity getEnforcer(long enforcerId);

    List<EnforcerEntity> searchEnforcers(EnforcerSearchDto criteria);
}
