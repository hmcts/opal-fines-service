package uk.gov.hmcts.opal.disco.opal;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.authorisation.model.BusinessUnitUser;
import uk.gov.hmcts.opal.dto.search.BusinessUnitUserSearchDto;
import uk.gov.hmcts.opal.entity.BusinessUnitUserEntity;
import uk.gov.hmcts.opal.repository.BusinessUnitUserRepository;
import uk.gov.hmcts.opal.repository.jpa.BusinessUnitUserSpecs;
import uk.gov.hmcts.opal.disco.BusinessUnitUserServiceInterface;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "opal.BusinessUnitUserService")
@Qualifier("businessUnitUserService")
public class BusinessUnitUserService implements BusinessUnitUserServiceInterface {

    private final BusinessUnitUserRepository businessUnitUserRepository;

    private final BusinessUnitUserSpecs specs = new BusinessUnitUserSpecs();

    @Override
    @Transactional(readOnly = true)
    public BusinessUnitUserEntity getBusinessUnitUser(String businessUnitUserId) {
        return businessUnitUserRepository.getReferenceById(businessUnitUserId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BusinessUnitUserEntity> searchBusinessUnitUsers(BusinessUnitUserSearchDto criteria) {
        Page<BusinessUnitUserEntity> page = businessUnitUserRepository
            .findBy(specs.findBySearchCriteria(criteria),
                    ffq -> ffq.page(Pageable.unpaged()));

        return page.getContent();
    }

    /**
     * Return a Set of 'cut down' Authorisation Business Unit Permissions mapped from Business Unit Users keyed on the
     * user id. This method is assuming that there are no Permissions for the Business Unit Users and so skips
     * performing the additional repository queries that <i>do</i> get performed in the method above.
     */
    @Transactional(readOnly = true)
    public Set<BusinessUnitUser> getLimitedBusinessUnitPermissionsByUserId(Long userId) {
        List<BusinessUnitUserEntity> buuList =  businessUnitUserRepository.findAllByUser_UserId(userId);

        return buuList.stream().map(buu -> BusinessUnitUser.builder()
            .businessUnitUserId(buu.getBusinessUnitUserId())
            .businessUnitId(buu.getBusinessUnit().getBusinessUnitId())
            .permissions(Collections.emptySet()) // Assuming that Permissions exist for BusinessUnitUser.
            .build()).collect(Collectors.toSet());

    }

}
