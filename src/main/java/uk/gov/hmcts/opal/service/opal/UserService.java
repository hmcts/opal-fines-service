package uk.gov.hmcts.opal.service.opal;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.search.UserSearchDto;
import uk.gov.hmcts.opal.entity.UserEntity;
import uk.gov.hmcts.opal.repository.UserRepository;
import uk.gov.hmcts.opal.repository.jpa.UserSpecs;
import uk.gov.hmcts.opal.service.UserServiceInterface;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "UserService")
@Qualifier("userService")
public class UserService implements UserServiceInterface {

    private final UserRepository userRepository;

    private final BusinessUnitUserService businessUnitUserService;

    private final UserSpecs specs = new UserSpecs();

    @Override
    public UserEntity getUser(String userId) {
        return userRepository.getReferenceById(userId);
    }

    @Override
    public List<UserEntity> searchUsers(UserSearchDto criteria) {
        Page<UserEntity> page = userRepository
            .findBy(specs.findBySearchCriteria(criteria),
                    ffq -> ffq.page(Pageable.unpaged()));

        return page.getContent();
    }

    /**
     * Retrieves a UserState object by starting with multiple queries against 3 different repositories.
     * During some limited developer testing, this method was less performant than the similar method
     * in the UserEntitlementService, but will still return a UserState even if no Entitlements exist for that user,
     * but the User <i>does</i> exist in the table.
     */
    public UserState getUserStateByUsername(String username) {
        UserEntity user = userRepository.findByUsername(username);
        return UserState.builder()
            .userId(user.getUserId())
            .userName(user.getUsername())
            .roles(businessUnitUserService.getAuthorisationRolesByUserId(user.getUserId()))
            .build();
    }

    /**
     * Return a 'cut down' UserState object that that only tries to populate Roles but not Permissions.
     * The assumption is that previous code has attempted to retrieve a UserState object via a query against
     * the UserEntitlementService, but failed. This could be because of a lack of Entitlements associated with
     * a BusinessUnitUnit, or a lack of BusinessUnitUsers associated with this user. So assuming there
     * is a valid User for the given username, then this method will return a non-null object.
     */
    public Optional<UserState> getLimitedUserStateByUsername(String username) {
        Optional<UserEntity> userEntity = Optional.ofNullable(userRepository.findByUsername(username));

        return userEntity.map(u -> UserState.builder()
            .userId(u.getUserId())
            .userName(u.getUsername())
            .roles(businessUnitUserService.getLimitedRolesByUserId(u.getUserId()))
            .build());
    }
}
