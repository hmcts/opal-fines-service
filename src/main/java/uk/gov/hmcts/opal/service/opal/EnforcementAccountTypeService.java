package uk.gov.hmcts.opal.service.opal;

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

import java.util.List;

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
        return mapper.toDtos(enforcementAccountTypes);
    }

    private void checkPermissions() {
        UserState userState = userStateService.checkForAuthorisedUser();
        // TODO this needs to check auto enforcement permission not enter forcement but waiting for PO-2451
        if (!userState.anyBusinessUnitUserHasPermission(FinesPermission.ENTER_ENFORCEMENT)) {
            throw new PermissionNotAllowedException(FinesPermission.ENTER_ENFORCEMENT);
        }
    }

}
