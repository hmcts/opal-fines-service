package uk.gov.hmcts.opal.service.opal;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.MajorCreditorSearchDto;
import uk.gov.hmcts.opal.entity.MajorCreditorEntity;
import uk.gov.hmcts.opal.entity.MajorCreditorEntity_;
import uk.gov.hmcts.opal.entity.projection.MajorCreditorReferenceData;
import uk.gov.hmcts.opal.repository.MajorCreditorRepository;
import uk.gov.hmcts.opal.repository.jpa.MajorCreditorSpecs;
import uk.gov.hmcts.opal.service.MajorCreditorServiceInterface;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j(topic = "MajorCreditorService")
@RequiredArgsConstructor
@Qualifier("majorCreditorService")
public class MajorCreditorService implements MajorCreditorServiceInterface {

    private final MajorCreditorRepository majorCreditorRepository;

    private final MajorCreditorSpecs specs = new MajorCreditorSpecs();

    @Override
    public MajorCreditorEntity getMajorCreditor(long majorCreditorId) {
        return majorCreditorRepository.getReferenceById(majorCreditorId);
    }

    @Override
    public List<MajorCreditorEntity> searchMajorCreditors(MajorCreditorSearchDto criteria) {
        Page<MajorCreditorEntity> page = majorCreditorRepository
            .findBy(specs.findBySearchCriteria(criteria),
                    ffq -> ffq.page(Pageable.unpaged()));

        return page.getContent();
    }

    @Cacheable(
        cacheNames = "majorCreditorReferenceDataCache",
        key = "#filter.orElse('noFilter') + '_' + #businessUnitId.orElse(0)"
    )
    public List<MajorCreditorReferenceData> getReferenceData(Optional<String> filter, Optional<Short> businessUnitId) {

        log.info(":getReferenceData: filter: {}, businessUnitId: {}", filter, businessUnitId);
        Sort nameSort = Sort.by(Sort.Direction.ASC, MajorCreditorEntity_.NAME);

        Page<MajorCreditorEntity> page = majorCreditorRepository
            .findBy(specs.referenceDataFilter(filter, businessUnitId),
                    ffq -> ffq
                        .sortBy(nameSort)
                        .page(Pageable.unpaged()));

        return page.getContent().stream().map(this::toRefData).toList();
    }

    private MajorCreditorReferenceData toRefData(MajorCreditorEntity entity) {
        log.info(":toRefData: entity: {}", entity);
        MajorCreditorReferenceData m = new MajorCreditorReferenceData(
            entity.getMajorCreditorId(),
            entity.getBusinessUnit().getBusinessUnitId(),
            entity.getMajorCreditorCode(),
            entity.getName(),
            entity.getPostcode()
        );
        log.info(":toRefData: refData: \n{}", m.toPrettyJson());
        return m;
    }
}
