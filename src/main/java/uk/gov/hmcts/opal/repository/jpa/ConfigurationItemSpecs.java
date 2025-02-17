package uk.gov.hmcts.opal.repository.jpa;

import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.search.ConfigurationItemSearchDto;
import uk.gov.hmcts.opal.entity.ConfigurationItemLite;
import uk.gov.hmcts.opal.entity.ConfigurationItemLite_;

public class ConfigurationItemSpecs extends EntitySpecs<ConfigurationItemLite> {

    public Specification<ConfigurationItemLite> findBySearchCriteria(ConfigurationItemSearchDto criteria) {
        return Specification.allOf(specificationList(
            notBlank(criteria.getConfigurationItemId()).map(ConfigurationItemSpecs::equalsConfigurationItemId),
            numericShort(criteria.getBusinessUnitId()).map(ConfigurationItemSpecs::equalsBusinessUnitId),
            notBlank(criteria.getItemName()).map(ConfigurationItemSpecs::equalsItemName),
            notBlank(criteria.getItemValue()).map(ConfigurationItemSpecs::equalsItemValue)
        ));
    }

    public static Specification<ConfigurationItemLite> equalsConfigurationItemId(String configurationItemId) {
        return (root, query, builder) -> builder.equal(root.get(ConfigurationItemLite_.configurationItemId),
                                                       configurationItemId);
    }

    public static Specification<ConfigurationItemLite> equalsBusinessUnitId(Short businessUnitId) {
        return (root, query, builder) ->
            // equalsBusinessUnitIdPredicate(joinBusinessUnit(root), builder, businessUnitId);
            builder.equal(root.get(ConfigurationItemLite_.businessUnitId), businessUnitId);
    }

    public static Specification<ConfigurationItemLite> equalsItemName(String itemName) {
        return (root, query, builder) -> builder.equal(root.get(ConfigurationItemLite_.itemName), itemName);
    }

    public static Specification<ConfigurationItemLite> equalsItemValue(String itemValue) {
        return (root, query, builder) -> builder.equal(root.get(ConfigurationItemLite_.itemValue), itemValue);
    }

    // public static Join<ConfigurationItemEntity, BusinessUnitEntity> joinBusinessUnit(
    //     From<?, ConfigurationItemEntity> from) {
    //     return from.join(ConfigurationItemEntity_.businessUnit);
    // }
}
