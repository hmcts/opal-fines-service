package uk.gov.hmcts.opal.service.opal;


import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.reference.BusinessUnitReferenceData;
import uk.gov.hmcts.opal.dto.search.BusinessUnitSearchDto;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntityLite;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntityLite_;
import uk.gov.hmcts.opal.entity.configurationitem.ConfigurationItemEntityLite;
import uk.gov.hmcts.opal.repository.BusinessUnitLiteRepository;
import uk.gov.hmcts.opal.repository.BusinessUnitRepository;
import uk.gov.hmcts.opal.repository.jpa.BusinessUnitLiteSpecs;
import uk.gov.hmcts.opal.repository.jpa.BusinessUnitSpecs;
import uk.gov.hmcts.opal.disco.BusinessUnitServiceInterface;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Qualifier("businessUnitService")
public class BusinessUnitService implements BusinessUnitServiceInterface {

    private final BusinessUnitRepository businessUnitRepository;

    private final BusinessUnitLiteRepository businessUnitLiteRepository;

    private final BusinessUnitSpecs specs = new BusinessUnitSpecs();

    private final BusinessUnitLiteSpecs liteSpecs = new BusinessUnitLiteSpecs();

    @Override
    public BusinessUnitEntity getBusinessUnit(short businessUnitId) {
        return businessUnitRepository.findById(businessUnitId)
            .orElseThrow(() -> new EntityNotFoundException("Business Unit not found with id: " + businessUnitId));
    }

    @Override
    public List<BusinessUnitEntity> searchBusinessUnits(BusinessUnitSearchDto criteria) {

        Page<BusinessUnitEntity> page = businessUnitRepository
            .findBy(specs.findBySearchCriteria(criteria),
                    ffq -> ffq.page(Pageable.unpaged()));

        return page.getContent();
    }

    @Cacheable(cacheNames = "businessUnitReferenceDataCache", key = "#filter.orElse('noFilter')")
    public List<BusinessUnitReferenceData> getReferenceData(Optional<String> filter) {

        Sort nameSort = Sort.by(Sort.Direction.ASC, BusinessUnitEntityLite_.BUSINESS_UNIT_NAME);

        Page<BusinessUnitEntityLite> page = businessUnitLiteRepository
            .findBy(liteSpecs.referenceDataFilter(filter),
                    ffq -> ffq
                        .sortBy(nameSort)
                        .page(Pageable.unpaged()));

        return page.getContent().stream().map(this::toRefData).toList();
    }

    private BusinessUnitReferenceData toRefData(BusinessUnitEntityLite entity) {
        return new BusinessUnitReferenceData(
            entity.getBusinessUnitId(),
            entity.getBusinessUnitName(),
            entity.getBusinessUnitCode(),
            entity.getBusinessUnitType(),
            entity.getAccountNumberPrefix(),
            entity.getOpalDomain(),
            entity.getWelshLanguage(),
            toRefData(entity.getConfigurationItems())
        );
    }

    private List<BusinessUnitReferenceData.ConfigItemRefData> toRefData(List<ConfigurationItemEntityLite> list) {
        return Optional.ofNullable(list).map(items -> items.stream().map(this::toRefData).toList()).orElse(null);
    }

    private BusinessUnitReferenceData.ConfigItemRefData toRefData(ConfigurationItemEntityLite entity) {
        return new BusinessUnitReferenceData.ConfigItemRefData(
            entity.getItemName(),
            entity.getItemValue(),
            entity.getItemValues()
        );
    }

}
