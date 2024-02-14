package uk.gov.hmcts.opal.service.opal;


import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.BusinessUnitSearchDto;
import uk.gov.hmcts.opal.entity.BusinessUnitEntity;
import uk.gov.hmcts.opal.repository.BusinessUnitRepository;
import uk.gov.hmcts.opal.repository.jpa.BusinessUnitSpecs;
import uk.gov.hmcts.opal.service.BusinessUnitServiceInterface;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BusinessUnitService implements BusinessUnitServiceInterface {

    private final BusinessUnitRepository businessUnitRepository;

    private final BusinessUnitSpecs specs = new BusinessUnitSpecs();

    @Override
    public BusinessUnitEntity getBusinessUnit(long businessUnitId) {
        return businessUnitRepository.getReferenceById(businessUnitId);
    }

    @Override
    public List<BusinessUnitEntity> searchBusinessUnits(BusinessUnitSearchDto criteria) {
        Page<BusinessUnitEntity> page = businessUnitRepository
            .findBy(specs.findBySearchCriteria(criteria),
                    ffq -> ffq.page(Pageable.unpaged()));

        return page.getContent();
    }

}
