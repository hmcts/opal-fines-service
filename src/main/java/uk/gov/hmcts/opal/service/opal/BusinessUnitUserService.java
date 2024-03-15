package uk.gov.hmcts.opal.service.opal;


import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.authorisation.model.Role;
import uk.gov.hmcts.opal.dto.search.BusinessUnitUserSearchDto;
import uk.gov.hmcts.opal.entity.BusinessUnitUserEntity;
import uk.gov.hmcts.opal.repository.BusinessUnitUserRepository;
import uk.gov.hmcts.opal.repository.jpa.BusinessUnitUserSpecs;
import uk.gov.hmcts.opal.service.BusinessUnitUserServiceInterface;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BusinessUnitUserService implements BusinessUnitUserServiceInterface {

    private final BusinessUnitUserRepository businessUnitUserRepository;

    private final UserEntitlementService userEntitlementService;

    private final BusinessUnitUserSpecs specs = new BusinessUnitUserSpecs();

    @Override
    public BusinessUnitUserEntity getBusinessUnitUser(String businessUnitUserId) {
        return businessUnitUserRepository.getReferenceById(businessUnitUserId);
    }

    @Override
    public List<BusinessUnitUserEntity> searchBusinessUnitUsers(BusinessUnitUserSearchDto criteria) {
        Page<BusinessUnitUserEntity> page = businessUnitUserRepository
            .findBy(specs.findBySearchCriteria(criteria),
                    ffq -> ffq.page(Pageable.unpaged()));

        return page.getContent();
    }

    /**
     * Return a Set of Authorisation Roles mapped from BusinessUnitUsers keyed on the user id from the Users table.
     */
    public Set<Role> getAuthorisationRolesByUserId(Long userId) {
        List<BusinessUnitUserEntity> buuList =  businessUnitUserRepository.findAllByUser_UserId(userId);

        return buuList.stream().map(buu -> Role.builder()
            .businessUserId(buu.getBusinessUnitUserId())
            .businessUnitId(buu.getBusinessUnit().getBusinessUnitId())
            .permissions(userEntitlementService.getPermissionsByBusinessUnitUserId(buu.getBusinessUnitUserId()))
            .build()).collect(Collectors.toSet());

    }

    /**
     * Return a Set of 'cut down' Authorisation Roles mapped from BusinessUnitUsers keyed on the user id.
     * This method is assuming that there are no Permissions for the Roles and so skips performing the additional
     * repository queries that <i>do</i> get performed in the method above.
     */
    public Set<Role> getLimitedRolesByUserId(Long userId) {
        List<BusinessUnitUserEntity> buuList =  businessUnitUserRepository.findAllByUser_UserId(userId);

        return buuList.stream().map(buu -> Role.builder()
            .businessUserId(buu.getBusinessUnitUserId())
            .businessUnitId(buu.getBusinessUnit().getBusinessUnitId())
            .permissions(Collections.emptySet()) // We are assuming that Permissions exist for this Role.
            .build()).collect(Collectors.toSet());

    }

}
