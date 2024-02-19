package uk.gov.hmcts.opal.service.opal;


import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.UserEntitlementSearchDto;
import uk.gov.hmcts.opal.entity.UserEntitlementEntity;
import uk.gov.hmcts.opal.repository.UserEntitlementRepository;
import uk.gov.hmcts.opal.repository.jpa.UserEntitlementSpecs;
import uk.gov.hmcts.opal.service.UserEntitlementServiceInterface;

import java.util.List;

@Service
@RequiredArgsConstructor
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

}
