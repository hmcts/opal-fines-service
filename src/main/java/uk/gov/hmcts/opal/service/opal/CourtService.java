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
import uk.gov.hmcts.opal.entity.court.CourtEntityFull;
import uk.gov.hmcts.opal.entity.projection.CourtReferenceData;
import uk.gov.hmcts.opal.repository.CourtRepository;
import uk.gov.hmcts.opal.repository.CourtRepositoryFull;
import uk.gov.hmcts.opal.repository.jpa.CourtSpecs;
import uk.gov.hmcts.opal.service.CourtServiceInterface;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j(topic = "opal.CourtService")
@RequiredArgsConstructor
@Qualifier("courtService")
public class CourtService implements CourtServiceInterface {

    private final CourtRepository courtRepository;

    private final CourtRepositoryFull courtRepositoryFull;

    private final CourtSpecs specs = new CourtSpecs();

    @Override
    public CourtEntity.Lite getCourtLite(long courtId) {
        log.info(":getCourtLite:\n");
        log.info(":getCourtLite: get id: {}", courtId);
        try {
            return courtRepository.findById(courtId)
                .orElseThrow(() -> new EntityNotFoundException("Court not found with id: "
                                                                   + courtId));
        } finally {
            log.info(":getCourtLite: got id: {}\n", courtId);
        }
    }

    public CourtEntityFull getCourtFull(long courtId) {
        log.info(":getCourtFull:\n");
        log.info(":getCourtFull: get id: {}", courtId);
        try {
            return courtRepositoryFull.findById(courtId)
                .orElseThrow(() -> new EntityNotFoundException("Court not found with id: "
                                                                   + courtId));
        } finally {
            log.info(":getCourtFull: got id: {}\n", courtId);
        }
    }

    @Override
    public List<CourtEntity.Lite> searchCourts(CourtSearchDto criteria) {
        Page<CourtEntity.Lite> courtsPage = courtRepository
            .findBy(specs.findBySearchCriteria(criteria), ffq -> ffq.page(Pageable.unpaged()));
        return courtsPage.getContent();
    }

    @Cacheable(
        value = "courtReferenceDataCache",
        key = "#filter.orElse('noFilter') + '_' + #businessUnitId.orElse(0)"
    )
    public List<CourtReferenceData> getReferenceData(Optional<String> filter, Optional<Short> businessUnitId) {

        Sort codeSort = Sort.by(Sort.Direction.ASC, AddressEntity_.NAME);

        Page<CourtEntity.Lite> page = courtRepository
            .findBy(specs.referenceDataFilter(filter, businessUnitId),
                    ffq -> ffq
                        .sortBy(codeSort)
                        .page(Pageable.unpaged()));

        return page.getContent().stream().map(this::toRefData).toList();
    }

    private CourtReferenceData toRefData(CourtEntity entity) {
        return new CourtReferenceData(
            entity.getCourtId(),
            entity.getBusinessUnitId(),
            entity.getCourtCode(),
            entity.getName(),
            entity.getNameCy(),
            entity.getNationalCourtCode()
        );
    }
}
