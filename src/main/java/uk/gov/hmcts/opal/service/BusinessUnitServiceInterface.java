package uk.gov.hmcts.opal.service;

import uk.gov.hmcts.opal.dto.search.BusinessUnitSearchDto;
import uk.gov.hmcts.opal.entity.BusinessUnitEntity;

import java.util.List;

public interface BusinessUnitServiceInterface {

    BusinessUnitEntity getBusinessUnit(long businessUnitId);

    List<BusinessUnitEntity> searchBusinessUnits(BusinessUnitSearchDto criteria);
}
