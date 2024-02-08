package uk.gov.hmcts.opal.service.opal;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.DebtorDetailSearchDto;
import uk.gov.hmcts.opal.entity.DebtorDetailEntity;
import uk.gov.hmcts.opal.repository.DebtorDetailRepository;
import uk.gov.hmcts.opal.service.DebtorDetailServiceInterface;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DebtorDetailService implements DebtorDetailServiceInterface {

    private final DebtorDetailRepository debtorDetailRepository;

    @Override
    public DebtorDetailEntity getDebtorDetail(long debtorDetailId) {
        return debtorDetailRepository.getReferenceById(debtorDetailId);
    }

    @Override
    public List<DebtorDetailEntity> searchDebtorDetails(DebtorDetailSearchDto criteria) {
        return null;
    }

}
