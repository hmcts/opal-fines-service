package uk.gov.hmcts.opal.service.opal;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.BacsPaymentSearchDto;
import uk.gov.hmcts.opal.entity.BacsPaymentEntity;
import uk.gov.hmcts.opal.repository.BacsPaymentRepository;
import uk.gov.hmcts.opal.repository.jpa.BacsPaymentSpecs;
import uk.gov.hmcts.opal.service.BacsPaymentServiceInterface;

import java.util.List;

@Service
@RequiredArgsConstructor
@Qualifier("bacsPaymentService")
public class BacsPaymentService implements BacsPaymentServiceInterface {

    private final BacsPaymentRepository bacsPaymentRepository;

    private final BacsPaymentSpecs specs = new BacsPaymentSpecs();

    @Override
    public BacsPaymentEntity getBacsPayment(long bacsPaymentId) {
        return bacsPaymentRepository.getReferenceById(bacsPaymentId);
    }

    @Override
    public List<BacsPaymentEntity> searchBacsPayments(BacsPaymentSearchDto criteria) {
        Page<BacsPaymentEntity> page = bacsPaymentRepository
            .findBy(specs.findBySearchCriteria(criteria),
                    ffq -> ffq.page(Pageable.unpaged()));

        return page.getContent();
    }

}
