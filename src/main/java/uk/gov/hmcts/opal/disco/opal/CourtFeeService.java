package uk.gov.hmcts.opal.disco.opal;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.CourtFeeSearchDto;
import uk.gov.hmcts.opal.entity.CourtFeeEntity;
import uk.gov.hmcts.opal.repository.CourtFeeRepository;
import uk.gov.hmcts.opal.repository.jpa.CourtFeeSpecs;
import uk.gov.hmcts.opal.disco.CourtFeeServiceInterface;

import java.util.List;

@Service
@RequiredArgsConstructor
@Qualifier("courtFeeService")
public class CourtFeeService implements CourtFeeServiceInterface {

    private final CourtFeeRepository courtFeeRepository;

    private final CourtFeeSpecs specs = new CourtFeeSpecs();

    @Override
    public CourtFeeEntity getCourtFee(long courtFeeId) {
        return courtFeeRepository.getReferenceById(courtFeeId);
    }

    @Override
    public List<CourtFeeEntity> searchCourtFees(CourtFeeSearchDto criteria) {
        Page<CourtFeeEntity> page = courtFeeRepository
            .findBy(specs.findBySearchCriteria(criteria),
                    ffq -> ffq.page(Pageable.unpaged()));

        return page.getContent();
    }

}
