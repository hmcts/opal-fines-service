package uk.gov.hmcts.opal.service.proxy;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.UserSearchDto;
import uk.gov.hmcts.opal.entity.UserEntity;
import uk.gov.hmcts.opal.service.DynamicConfigService;
import uk.gov.hmcts.opal.service.UserServiceInterface;
import uk.gov.hmcts.opal.service.legacy.LegacyUserService;
import uk.gov.hmcts.opal.service.opal.UserService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Qualifier("userServiceProxy")
public class UserServiceProxy implements UserServiceInterface, ProxyInterface {

    private final UserService opalUserService;
    private final LegacyUserService legacyUserService;
    private final DynamicConfigService dynamicConfigService;

    private UserServiceInterface getCurrentModeService() {
        return isLegacyMode(dynamicConfigService) ? legacyUserService : opalUserService;
    }

    @Override
    public UserEntity getUser(String userId) {
        return getCurrentModeService().getUser(userId);
    }

    @Override
    public List<UserEntity> searchUsers(UserSearchDto criteria) {
        return getCurrentModeService().searchUsers(criteria);
    }
}
