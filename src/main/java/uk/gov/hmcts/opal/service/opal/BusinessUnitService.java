package uk.gov.hmcts.opal.service.opal;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.BusinessUnitSearchDto;
import uk.gov.hmcts.opal.entity.ConfigurationItemLite;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnit;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitCore;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitCore_;
import uk.gov.hmcts.opal.entity.projection.BusinessUnitReferenceData;
import uk.gov.hmcts.opal.repository.BusinessUnitCoreRepository;
import uk.gov.hmcts.opal.repository.BusinessUnitLiteRepository;
import uk.gov.hmcts.opal.repository.jpa.BusinessUnitCoreSpecs;
import uk.gov.hmcts.opal.repository.jpa.BusinessUnitLiteSpecs;
import uk.gov.hmcts.opal.service.BusinessUnitServiceInterface;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Qualifier("businessUnitService")
public class BusinessUnitService implements BusinessUnitServiceInterface {

    private final BusinessUnitLiteRepository businessUnitLiteRepository;

    private final BusinessUnitCoreRepository businessUnitCoreRepository;

    private final BusinessUnitCoreSpecs specsCore = new BusinessUnitCoreSpecs();
    private final BusinessUnitLiteSpecs specsLite = new BusinessUnitLiteSpecs();

    @Override
    public BusinessUnitCore getBusinessUnit(short businessUnitId) {
        return businessUnitCoreRepository.getReferenceById(businessUnitId);
    }

    @Override
    public List<BusinessUnit.Lite> searchBusinessUnits(BusinessUnitSearchDto criteria) {
        Page<BusinessUnit.Lite> page = businessUnitLiteRepository
            .findBy(specsLite.findBySearchCriteria(criteria),
                    ffq -> ffq.page(Pageable.unpaged()));

        return page.getContent();
    }

    @Cacheable(cacheNames = "businessUnitLiteReferenceDataCache", key = "#filter.orElse('noFilter')")
    public List<BusinessUnitReferenceData> getReferenceData(Optional<String> filter) {

        Sort nameSort = Sort.by(Sort.Direction.ASC, BusinessUnitCore_.BUSINESS_UNIT_NAME);

        Page<BusinessUnitCore> page = businessUnitCoreRepository
            .findBy(specsCore.referenceDataFilter(filter),
                    ffq -> ffq
                        .sortBy(nameSort)
                        .page(Pageable.unpaged()));

        return page.getContent().stream().map(this::toRefData).toList();
    }

    private BusinessUnitReferenceData toRefData(BusinessUnitCore entity) {
        return new BusinessUnitReferenceData(
            entity.getBusinessUnitId(),
            entity.getBusinessUnitName(),
            entity.getBusinessUnitCode(),
            entity.getBusinessUnitType(),
            entity.getAccountNumberPrefix(),
            entity.getOpalDomain(),
            entity.getWelshLanguage(),
            toRefDataLite(entity.getConfigurationItems())
        );
    }

    private BusinessUnitReferenceData.ConfigItemRefData toRefData(ConfigurationItemLite entity) {
        return new BusinessUnitReferenceData.ConfigItemRefData(
            entity.getItemName(),
            entity.getItemValue(),
            entity.getItemValues()
        );
    }

    private List<BusinessUnitReferenceData.ConfigItemRefData> toRefDataLite(List<ConfigurationItemLite> list) {
        return Optional.ofNullable(list).map(items -> items.stream().map(this::toRefData).toList()).orElse(null);
    }

}
