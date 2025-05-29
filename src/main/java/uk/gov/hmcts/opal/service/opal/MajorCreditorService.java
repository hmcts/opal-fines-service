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
import uk.gov.hmcts.opal.dto.search.MajorCreditorSearchDto;
import uk.gov.hmcts.opal.entity.MajorCreditorEntity;
import uk.gov.hmcts.opal.entity.MajorCreditorEntity_;
import uk.gov.hmcts.opal.entity.projection.MajorCreditorReferenceData;
import uk.gov.hmcts.opal.repository.MajorCreditorRepository;
import uk.gov.hmcts.opal.repository.jpa.MajorCreditorSpecs;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j(topic = "opal.MajorCreditorService")
@RequiredArgsConstructor
@Qualifier("majorCreditorService")
public class MajorCreditorService {

    private final MajorCreditorRepository majorCreditorRepository;

    private final MajorCreditorSpecs specs = new MajorCreditorSpecs();

    public MajorCreditorEntity getMajorCreditorById(long majorCreditorId) {
        return majorCreditorRepository.findById(majorCreditorId)
            .orElseThrow(() -> new EntityNotFoundException("Court not found with id: " + majorCreditorId));
    }

    public List<MajorCreditorEntity> searchMajorCreditors(MajorCreditorSearchDto criteria) {

        Sort nameSort = Sort.by(Sort.Direction.ASC, MajorCreditorEntity_.NAME);

        Page<MajorCreditorEntity> page = majorCreditorRepository
            .findBy(specs.findBySearchCriteria(criteria),
                    ffq -> ffq
                        .sortBy(nameSort)
                        .page(Pageable.unpaged()));

        return page.getContent();
    }

    @Cacheable(
        cacheNames = "majorCreditorReferenceDataCache",
        key = "#filter.orElse('noFilter') + '_' + #businessUnitId.orElse(0)"
    )
    public List<MajorCreditorReferenceData> getReferenceData(Optional<String> filter, Optional<Short> businessUnitId) {

        log.debug(":getReferenceData: filter: {}, businessUnitId: {}", filter, businessUnitId);
        Sort nameSort = Sort.by(Sort.Direction.ASC, MajorCreditorEntity_.NAME);

        Page<MajorCreditorEntity> page = majorCreditorRepository
            .findBy(specs.referenceDataFilter(filter, businessUnitId),
                    ffq -> ffq
                        .sortBy(nameSort)
                        .page(Pageable.unpaged()));

        return page.getContent().stream().map(this::toRefData).toList();
    }

    private MajorCreditorReferenceData toRefData(MajorCreditorEntity entity) {
        log.debug(":toRefData: entity: {}", entity);

        MajorCreditorReferenceData.MajorCreditorReferenceDataBuilder builder = MajorCreditorReferenceData.builder()
            .majorCreditorId(entity.getMajorCreditorId())
            .businessUnitId(entity.getBusinessUnit().getBusinessUnitId())
            .majorCreditorCode(entity.getMajorCreditorCode())
            .name(entity.getName())
            .postcode(entity.getPostcode());

        MajorCreditorReferenceData mcrd = Optional.ofNullable(entity.getCreditorAccountEntity())
            .map(cae -> builder
                .creditorAccountId(cae.getCreditorAccountId())
                .accountNumber(cae.getAccountsNumber())
                .creditorAccountType(cae.getCreditorAccountType())
                .prosecutionService(cae.isProsecutionService())
                .minorCreditorPartyId(cae.getMinorCreditorPartyId())
                .fromSuspense(cae.isFromSuspense())
                .holdPayout(cae.isHoldPayout())
                .lastChangedDate(cae.getLastChangedDate())
                .build())
            .orElse(builder.build());

        log.debug(":toRefData: refData: \n{}", mcrd.toPrettyJson());
        return mcrd;
    }
}
