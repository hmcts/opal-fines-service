package uk.gov.hmcts.opal.disco;

import uk.gov.hmcts.opal.dto.search.UserSearchDto;
import uk.gov.hmcts.opal.entity.UserEntity;

import java.util.List;

public interface UserServiceInterface {

    UserEntity getUser(String userId);

    List<UserEntity> searchUsers(UserSearchDto criteria);
}
