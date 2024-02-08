package uk.gov.hmcts.opal.service.opal;


import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.LocalJusticeAreaSearchDto;
import uk.gov.hmcts.opal.entity.LocalJusticeAreaEntity;
import uk.gov.hmcts.opal.repository.LocalJusticeAreaRepository;
import uk.gov.hmcts.opal.repository.jpa.LocalJusticeAreaSpecs;
import uk.gov.hmcts.opal.service.LocalJusticeAreaServiceInterface;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LocalJusticeAreaService implements LocalJusticeAreaServiceInterface {

    private final LocalJusticeAreaRepository localJusticeAreaRepository;

    private final LocalJusticeAreaSpecs specs = new LocalJusticeAreaSpecs();

    @Override
    public LocalJusticeAreaEntity getLocalJusticeArea(short localJusticeAreaId) {
        return localJusticeAreaRepository.getReferenceById(localJusticeAreaId);
    }

    @Override
    public List<LocalJusticeAreaEntity> searchLocalJusticeAreas(LocalJusticeAreaSearchDto criteria) {
        Page<LocalJusticeAreaEntity> page = localJusticeAreaRepository
            .findBy(specs.findBySearchCriteria(criteria),
                    ffq -> ffq.page(Pageable.unpaged()));

        return page.getContent();
    }

}
