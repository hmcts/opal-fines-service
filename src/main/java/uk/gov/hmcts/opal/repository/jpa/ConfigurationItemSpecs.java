package uk.gov.hmcts.opal.repository.jpa;

import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.search.ConfigurationItemSearchDto;
import uk.gov.hmcts.opal.entity.ConfigurationItemEntity;
import uk.gov.hmcts.opal.entity.ConfigurationItemEntity_;

public class ConfigurationItemSpecs extends EntitySpecs<ConfigurationItemEntity> {

    public Specification<ConfigurationItemEntity> findBySearchCriteria(ConfigurationItemSearchDto criteria) {
        return Specification.allOf(specificationList(
            notBlank(criteria.getConfigurationItemId()).map(ConfigurationItemSpecs::equalsConfigurationItemId),
            notBlank(criteria.getBusinessUnitId()).map(ConfigurationItemSpecs::equalsBusinessUnitId),
            notBlank(criteria.getItemName()).map(ConfigurationItemSpecs::equalsItemName),
            notBlank(criteria.getItemValue()).map(ConfigurationItemSpecs::equalsItemValue)
        ));
    }

    public static Specification<ConfigurationItemEntity> equalsConfigurationItemId(String configurationItemId) {
        return (root, query, builder) -> builder.equal(root.get(ConfigurationItemEntity_.configurationItemId),
                                                       configurationItemId);
    }

    public static Specification<ConfigurationItemEntity> equalsBusinessUnitId(String businessUnitId) {
        return (root, query, builder) -> builder.equal(root.get(ConfigurationItemEntity_.businessUnitId),
                                                       businessUnitId);
    }

    public static Specification<ConfigurationItemEntity> equalsItemName(String itemName) {
        return (root, query, builder) -> builder.equal(root.get(ConfigurationItemEntity_.itemName), itemName);
    }

    public static Specification<ConfigurationItemEntity> equalsItemValue(String itemValue) {
        return (root, query, builder) -> builder.equal(root.get(ConfigurationItemEntity_.itemValue), itemValue);
    }

}
