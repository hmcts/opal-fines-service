package uk.gov.hmcts.opal.service.opal;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.BusinessUnitSearchDto;
import uk.gov.hmcts.opal.entity.BusinessUnitEntity;
import uk.gov.hmcts.opal.repository.BusinessUnitRepository;
import uk.gov.hmcts.opal.service.BusinessUnitServiceInterface;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BusinessUnitService implements BusinessUnitServiceInterface {

    private final BusinessUnitRepository businessUnitRepository;

    @Override
    public BusinessUnitEntity getBusinessUnit(long businessUnitId) {
        return businessUnitRepository.getReferenceById(businessUnitId);
    }

    @Override
    public List<BusinessUnitEntity> searchBusinessUnits(BusinessUnitSearchDto criteria) {
        return null;
    }

}
