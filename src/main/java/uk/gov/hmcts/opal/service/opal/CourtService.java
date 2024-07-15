package uk.gov.hmcts.opal.service.opal;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.CourtSearchDto;
import uk.gov.hmcts.opal.entity.AddressEntity_;
import uk.gov.hmcts.opal.entity.CourtEntity;
import uk.gov.hmcts.opal.entity.projection.CourtReferenceData;
import uk.gov.hmcts.opal.repository.CourtRepository;
import uk.gov.hmcts.opal.repository.jpa.CourtSpecs;
import uk.gov.hmcts.opal.service.CourtServiceInterface;

import java.util.List;
import java.util.Optional;

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

    @Cacheable(
        value = "courtReferenceDataCache",
        key = "#filter.orElse('noFilter') + '_' + #businessUnitId.orElse(0)"
    )
    public List<CourtReferenceData> getReferenceData(Optional<String> filter, Optional<Short> businessUnitId) {

        Sort codeSort = Sort.by(Sort.Direction.ASC, AddressEntity_.NAME);

        Page<CourtEntity> page = courtRepository
            .findBy(specs.referenceDataFilter(filter, businessUnitId),
                    ffq -> ffq
                        .sortBy(codeSort)
                        .page(Pageable.unpaged()));

        return page.getContent().stream().map(this::toRefData).toList();
    }

    private CourtReferenceData toRefData(CourtEntity entity) {
        return new CourtReferenceData(
            entity.getCourtId(),
            entity.getBusinessUnit().getBusinessUnitId(),
            entity.getCourtCode(),
            entity.getName(),
            entity.getNameCy(),
            entity.getNationalCourtCode()
        );
    }
}
