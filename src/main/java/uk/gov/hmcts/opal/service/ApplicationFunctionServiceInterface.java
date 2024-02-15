package uk.gov.hmcts.opal.service;

import uk.gov.hmcts.opal.dto.search.ApplicationFunctionSearchDto;
import uk.gov.hmcts.opal.entity.ApplicationFunctionEntity;

import java.util.List;

public interface ApplicationFunctionServiceInterface {

    ApplicationFunctionEntity getApplicationFunction(long applicationFunctionId);

    List<ApplicationFunctionEntity> searchApplicationFunctions(ApplicationFunctionSearchDto criteria);
}
