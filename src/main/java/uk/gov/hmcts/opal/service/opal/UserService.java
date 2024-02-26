package uk.gov.hmcts.opal.service.opal;


import lombok.RequiredArgsConstructor;
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

@Service
@RequiredArgsConstructor
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

    public UserState getUserStateByUsername(String username) {
        UserEntity user = userRepository.findByUsername(username);
        return UserState.builder()
            .userId(user.getUserId())
            .userName(user.getUsername())
            .roles(businessUnitUserService.getAuthorisationRolesByUserId(user.getUserId()))
            .build();
    }

}
