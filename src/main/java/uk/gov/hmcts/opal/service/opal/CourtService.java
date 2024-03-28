package uk.gov.hmcts.opal.service.opal;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.CourtSearchDto;
import uk.gov.hmcts.opal.entity.CourtEntity;
import uk.gov.hmcts.opal.repository.CourtRepository;
import uk.gov.hmcts.opal.repository.jpa.CourtSpecs;
import uk.gov.hmcts.opal.service.CourtServiceInterface;

import java.util.List;

@Service
@RequiredArgsConstructor
@Qualifier("courtService")
public class CourtService implements CourtServiceInterface {

    private final CourtRepository courtRepository;

    private final CourtSpecs specs = new CourtSpecs();

    @Override
    public CourtEntity getCourt(long courtId) {
        return courtRepository.getReferenceById(courtId);
    }

    @Override
    public List<CourtEntity> searchCourts(CourtSearchDto criteria) {
        Page<CourtEntity> courtsPage = courtRepository
            .findBy(specs.findBySearchCriteria(criteria), ffq -> ffq.page(Pageable.unpaged()));
        return courtsPage.getContent();
    }

}
