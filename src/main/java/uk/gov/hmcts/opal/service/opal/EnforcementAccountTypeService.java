package uk.gov.hmcts.opal.service.opal;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.core.TypedPropertyPath;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.common.user.authorisation.exception.PermissionNotAllowedException;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.entity.enforcement.EnforcementAccountTypeEntity;
import uk.gov.hmcts.opal.generated.model.EnforcementAccountTypeCommon;
import uk.gov.hmcts.opal.mapper.EnforcementAccountTypeMapper;
import uk.gov.hmcts.opal.repository.EnforcementAccountTypeRepository;
import uk.gov.hmcts.opal.service.UserStateService;

@Service
@RequiredArgsConstructor
public class EnforcementAccountTypeService {

    private final UserStateService userStateService;
    private final EnforcementAccountTypeRepository repository;
    private final EnforcementAccountTypeMapper mapper;

    @Transactional(readOnly = true)
    public List<EnforcementAccountTypeCommon> getAllEnforcementAccountTypes() {
        checkPermissions();
        List<EnforcementAccountTypeEntity> enforcementAccountTypes = repository.findAll(
            Sort.by(Sort.Direction.ASC, TypedPropertyPath.of(EnforcementAccountTypeEntity::getEnforcementAccountTypeId))
        );
        return mapper.toEnforcementAccountTypeCommonList(enforcementAccountTypes);
    }

    private void checkPermissions() {
        UserState userState = userStateService.checkForAuthorisedUser();
        if (!userState.anyBusinessUnitUserHasPermission(FinesPermission.AUTO_ENFORCEMENT)) {
            throw new PermissionNotAllowedException(FinesPermission.AUTO_ENFORCEMENT);
        }
    }

}
