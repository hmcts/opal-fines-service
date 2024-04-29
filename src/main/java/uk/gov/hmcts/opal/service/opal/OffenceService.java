package uk.gov.hmcts.opal.service.opal;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.OffenceSearchDto;
import uk.gov.hmcts.opal.entity.OffenceEntity;
import uk.gov.hmcts.opal.entity.OffenceEntity_;
import uk.gov.hmcts.opal.entity.projection.OffenceReferenceData;
import uk.gov.hmcts.opal.repository.OffenceRepository;
import uk.gov.hmcts.opal.repository.jpa.OffenceSpecs;
import uk.gov.hmcts.opal.service.OffenceServiceInterface;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Qualifier("offenceService")
public class OffenceService implements OffenceServiceInterface {

    private final OffenceRepository offenceRepository;

    private final OffenceSpecs specs = new OffenceSpecs();

    @Override
    public OffenceEntity getOffence(long offenceId) {
        return offenceRepository.getReferenceById(offenceId);
    }

    @Override
    public List<OffenceEntity> searchOffences(OffenceSearchDto criteria) {
        Page<OffenceEntity> page = offenceRepository
            .findBy(specs.findBySearchCriteria(criteria),
                    ffq -> ffq.page(Pageable.unpaged()));

        return page.getContent();
    }

    public List<OffenceReferenceData> getReferenceData(Optional<String> filter) {

        Sort codeSort = Sort.by(Sort.Direction.ASC, OffenceEntity_.CJS_CODE);

        Page<OffenceReferenceData> page = offenceRepository
            .findBy(specs.referenceDataFilter(filter),
                    ffq -> ffq
                        .sortBy(codeSort)
                        .as(OffenceReferenceData.class)
                        .page(Pageable.unpaged()));

        return page.getContent();
    }
}
