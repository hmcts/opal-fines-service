package uk.gov.hmcts.opal.disco.opal;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.DebtorDetailSearchDto;
import uk.gov.hmcts.opal.entity.DebtorDetailEntity;
import uk.gov.hmcts.opal.repository.DebtorDetailRepository;
import uk.gov.hmcts.opal.repository.jpa.DebtorDetailSpecs;
import uk.gov.hmcts.opal.disco.DebtorDetailServiceInterface;

import java.util.List;

@Service
@RequiredArgsConstructor
@Qualifier("debtorDetailService")
public class DebtorDetailService implements DebtorDetailServiceInterface {

    private final DebtorDetailRepository debtorDetailRepository;

    private final DebtorDetailSpecs specs = new DebtorDetailSpecs();

    @Override
    public DebtorDetailEntity getDebtorDetail(long debtorDetailId) {
        return debtorDetailRepository.getReferenceById(debtorDetailId);
    }

    @Override
    public List<DebtorDetailEntity> searchDebtorDetails(DebtorDetailSearchDto criteria) {
        Page<DebtorDetailEntity> page = debtorDetailRepository
            .findBy(specs.findBySearchCriteria(criteria),
                    ffq -> ffq.page(Pageable.unpaged()));

        return page.getContent();
    }

}
