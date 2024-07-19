package uk.gov.hmcts.opal.repository.jpa;

import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.search.ConfigurationItemSearchDto;
import uk.gov.hmcts.opal.entity.BusinessUnitEntity;
import uk.gov.hmcts.opal.entity.ConfigurationItemEntity;
import uk.gov.hmcts.opal.entity.ConfigurationItemEntity_;

import static uk.gov.hmcts.opal.repository.jpa.BusinessUnitSpecs.equalsBusinessUnitIdPredicate;

public class ConfigurationItemSpecs extends EntitySpecs<ConfigurationItemEntity> {

    public Specification<ConfigurationItemEntity> findBySearchCriteria(ConfigurationItemSearchDto criteria) {
        return Specification.allOf(specificationList(
            notBlank(criteria.getConfigurationItemId()).map(ConfigurationItemSpecs::equalsConfigurationItemId),
            numericShort(criteria.getBusinessUnitId()).map(ConfigurationItemSpecs::equalsBusinessUnitId),
            notBlank(criteria.getItemName()).map(ConfigurationItemSpecs::equalsItemName),
            notBlank(criteria.getItemValue()).map(ConfigurationItemSpecs::equalsItemValue)
        ));
    }

    public static Specification<ConfigurationItemEntity> equalsConfigurationItemId(String configurationItemId) {
        return (root, query, builder) -> builder.equal(root.get(ConfigurationItemEntity_.configurationItemId),
                                                       configurationItemId);
    }

    public static Specification<ConfigurationItemEntity> equalsBusinessUnitId(Short businessUnitId) {
        return (root, query, builder) ->
            equalsBusinessUnitIdPredicate(joinBusinessUnit(root), builder, businessUnitId);
    }

    public static Specification<ConfigurationItemEntity> equalsItemName(String itemName) {
        return (root, query, builder) -> builder.equal(root.get(ConfigurationItemEntity_.itemName), itemName);
    }

    public static Specification<ConfigurationItemEntity> equalsItemValue(String itemValue) {
        return (root, query, builder) -> builder.equal(root.get(ConfigurationItemEntity_.itemValue), itemValue);
    }

    public static Join<ConfigurationItemEntity, BusinessUnitEntity> joinBusinessUnit(
        From<?, ConfigurationItemEntity> from) {
        return from.join(ConfigurationItemEntity_.businessUnit);
    }
}
