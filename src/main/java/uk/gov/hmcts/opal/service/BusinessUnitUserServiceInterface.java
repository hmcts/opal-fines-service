package uk.gov.hmcts.opal.service;

import uk.gov.hmcts.opal.dto.search.BusinessUnitUserSearchDto;
import uk.gov.hmcts.opal.entity.BusinessUnitUserEntity;

import java.util.List;

public interface BusinessUnitUserServiceInterface {

    BusinessUnitUserEntity getBusinessUnitUser(String businessUnitUserId);

    List<BusinessUnitUserEntity> searchBusinessUnitUsers(BusinessUnitUserSearchDto criteria);
}
