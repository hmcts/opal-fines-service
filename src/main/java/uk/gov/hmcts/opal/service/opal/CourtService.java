package uk.gov.hmcts.opal.service.opal;


import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.CourtSearchDto;
import uk.gov.hmcts.opal.entity.AddressEntity_;
import uk.gov.hmcts.opal.entity.court.CourtEntity;
import uk.gov.hmcts.opal.dto.reference.CourtReferenceData;
import uk.gov.hmcts.opal.mapper.CourtMapper;
import uk.gov.hmcts.opal.repository.CourtRepository;
import uk.gov.hmcts.opal.repository.jpa.CourtSpecs;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "opal.CourtService")
@Qualifier("courtService")
public class CourtService {

    private final CourtRepository courtRepository;

    private final CourtMapper courtMapper;

    private final CourtSpecs specs = new CourtSpecs();

    public CourtEntity getCourtById(long courtId) {
        return courtRepository.findById(courtId)
            .orElseThrow(() -> new EntityNotFoundException("Court not found with id: " + courtId));
    }

    public List<CourtEntity> searchCourts(CourtSearchDto criteria) {

        log.debug(":searchCourts: criteria: {}", criteria);
        Sort nameSort = Sort.by(Sort.Direction.ASC, AddressEntity_.NAME);

        Page<CourtEntity> courtsPage = courtRepository
            .findBy(specs.findBySearchCriteria(criteria),
                    ffq -> ffq
                        .sortBy(nameSort)
                        .page(Pageable.unpaged()));

        log.debug(":searchCourts: found count: {}", courtsPage.stream().count());
        return courtsPage.getContent();
    }

    @Cacheable(
        value = "courtReferenceDataCache",
        key = "#filter.orElse('noFilter') + '_' + #businessUnitId.orElse(0)"
    )
    public List<CourtReferenceData> getReferenceData(Optional<String> filter, Optional<Short> businessUnitId) {

        Sort nameSort = Sort.by(Sort.Direction.ASC, AddressEntity_.NAME);

        Page<CourtEntity> page = courtRepository
            .findBy(specs.referenceDataFilter(filter, businessUnitId),
                    ffq -> ffq
                        .sortBy(nameSort)
                        .page(Pageable.unpaged()));

        return page.getContent().stream().map(courtMapper::toRefData).toList();
    }

}
