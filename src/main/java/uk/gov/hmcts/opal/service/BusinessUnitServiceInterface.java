package uk.gov.hmcts.opal.service;

import uk.gov.hmcts.opal.dto.search.BusinessUnitSearchDto;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnit;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitCore;

import java.util.List;

public interface BusinessUnitServiceInterface {

    BusinessUnitCore getBusinessUnit(short businessUnitId);

    List<BusinessUnit.Lite> searchBusinessUnits(BusinessUnitSearchDto criteria);
}
