package uk.gov.hmcts.opal.disco.opal;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.HmrcRequestSearchDto;
import uk.gov.hmcts.opal.entity.HmrcRequestEntity;
import uk.gov.hmcts.opal.repository.HmrcRequestRepository;
import uk.gov.hmcts.opal.repository.jpa.HmrcRequestSpecs;
import uk.gov.hmcts.opal.disco.HmrcRequestServiceInterface;

import java.util.List;

@Service
@RequiredArgsConstructor
@Qualifier("hmrcRequestService")
public class HmrcRequestService implements HmrcRequestServiceInterface {

    private final HmrcRequestRepository hmrcRequestRepository;

    private final HmrcRequestSpecs specs = new HmrcRequestSpecs();

    @Override
    public HmrcRequestEntity getHmrcRequest(long hmrcRequestId) {
        return hmrcRequestRepository.getReferenceById(hmrcRequestId);
    }

    @Override
    public List<HmrcRequestEntity> searchHmrcRequests(HmrcRequestSearchDto criteria) {
        Page<HmrcRequestEntity> page = hmrcRequestRepository
            .findBy(specs.findBySearchCriteria(criteria),
                    ffq -> ffq.page(Pageable.unpaged()));

        return page.getContent();
    }

}
