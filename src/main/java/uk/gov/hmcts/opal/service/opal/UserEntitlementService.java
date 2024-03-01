package uk.gov.hmcts.opal.service.opal;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.authentication.exception.AuthenticationException;
import uk.gov.hmcts.opal.authorisation.model.Permission;
import uk.gov.hmcts.opal.authorisation.model.Role;
import uk.gov.hmcts.opal.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.search.UserEntitlementSearchDto;
import uk.gov.hmcts.opal.entity.BusinessUnitUserEntity;
import uk.gov.hmcts.opal.entity.UserEntitlementEntity;
import uk.gov.hmcts.opal.entity.UserEntity;
import uk.gov.hmcts.opal.repository.UserEntitlementRepository;
import uk.gov.hmcts.opal.repository.jpa.UserEntitlementSpecs;
import uk.gov.hmcts.opal.service.UserEntitlementServiceInterface;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toSet;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "UserEntitlementService")
public class UserEntitlementService implements UserEntitlementServiceInterface {

    private final UserEntitlementRepository userEntitlementRepository;

    private final UserEntitlementSpecs specs = new UserEntitlementSpecs();

    @Override
    public UserEntitlementEntity getUserEntitlement(long userEntitlementId) {
        return userEntitlementRepository.getReferenceById(userEntitlementId);
    }

    @Override
    public List<UserEntitlementEntity> searchUserEntitlements(UserEntitlementSearchDto criteria) {
        Page<UserEntitlementEntity> page = userEntitlementRepository
            .findBy(specs.findBySearchCriteria(criteria),
                    ffq -> ffq.page(Pageable.unpaged()));

        return page.getContent();
    }

    public Set<Permission> getPermissionsByBusinessUnitUserId(String businessUnitUserId) {
        return toPermissions(userEntitlementRepository
                                 .findAllByBusinessUnitUser_BusinessUnitUserId(businessUnitUserId));
    }

    /**
     * Retrieves a UserState object by starting with a query against the UserEntitlements.
     * During some limited developer testing, this method was more performant than the similar method
     * in the UserService, but won't return a UserState if no Entitlements exist for that user.
     */
    public Optional<UserState> getUserStateByUsername(String username) {

        List<UserEntitlementEntity> entitlements = userEntitlementRepository
            .findAll(UserEntitlementSpecs.equalsUsername(username));
        Set<BusinessUnitUserEntity> businessUnitUsers = entitlements.stream()
            .map(UserEntitlementEntity::getBusinessUnitUser).filter(Objects::nonNull).collect(toSet());
        Set<UserEntity> users = businessUnitUsers.stream().map(BusinessUnitUserEntity::getUser)
            .filter(Objects::nonNull).collect(toSet());

        if (users.size() > 1) {
            throw new AuthenticationException("Multiple Users matching username: " + username);
        }

        Map<String, List<UserEntitlementEntity>> entitlementsMap = entitlements.stream()
            .filter(e -> e.getBusinessUnitUserId() != null)
            .collect(Collectors.groupingBy(UserEntitlementEntity::getBusinessUnitUserId));

        return users.stream().findFirst().map(u -> UserState.builder()
            .userId(u.getUserId())
            .userName(u.getUsername())
            .roles(businessUnitUsers.stream().map(buu -> Role.builder()
                .businessUserId(buu.getBusinessUnitUserId())
                .businessUnit(buu.getBusinessUnit().getBusinessUnitId().toString())
                .permissions(toPermissions(entitlementsMap.get(buu.getBusinessUnitUserId())))
                .build()).collect(toSet()))
            .build());
    }

    private Set<Permission> toPermissions(List<UserEntitlementEntity> entitlements) {
        return entitlements
            .stream().map(uee -> Permission.builder()
                .permissionId(uee.getApplicationFunctionId())
                .permissionName(uee.getFunctionName())
                .build()).collect(toSet());
    }
}
