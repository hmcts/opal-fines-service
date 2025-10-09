package uk.gov.hmcts.opal.disco;

import uk.gov.hmcts.opal.dto.search.BusinessUnitSearchDto;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitFullEntity;

import java.util.List;

public interface BusinessUnitServiceInterface {

    BusinessUnitFullEntity getBusinessUnit(short businessUnitId);

    List<BusinessUnitFullEntity> searchBusinessUnits(BusinessUnitSearchDto criteria);
}
