package uk.gov.hmcts.opal.repository.jpa;

import jakarta.persistence.metamodel.SingularAttribute;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.search.AddressCySearch;
import uk.gov.hmcts.opal.entity.AddressCyEntity;
import uk.gov.hmcts.opal.entity.AddressCyEntity_;

public abstract class AddressCySpecs<E extends AddressCyEntity> extends AddressSpecs<E> {

    @SuppressWarnings("unchecked")
    public List<Optional<Specification<E>>> findByAddressCyCriteria(AddressCySearch criteria) {
        return combine(findByAddressCriteria(criteria),
            notBlank(criteria.getNameCy()).map(this::likeNameCy),
            notBlank(criteria.getAddressLineCy()).map(this::likeAnyAddressLineCy));
    }

    public Specification<E> likeNameCy(String nameCy) {
        return (root, query, builder) ->
            likeWildcardPredicate(root.get(AddressCyEntity_.nameCy), builder, nameCy);
    }

    public Specification<E> likeAnyAddressLineCy(String addressLine) {
        String addressLinePattern = "%" + addressLine.toLowerCase() + "%";
        return Specification.anyOf(
            addressLine1CyLike(addressLinePattern),
            addressLine2CyLike(addressLinePattern),
            addressLine3CyLike(addressLinePattern));
    }

    public Specification<E> addressLine1CyLike(String addressLinePattern) {
        return addressLineCyLike(AddressCyEntity_.addressLine1Cy, addressLinePattern);
    }

    public Specification<E> addressLine2CyLike(String addressLinePattern) {
        return addressLineCyLike(AddressCyEntity_.addressLine2Cy, addressLinePattern);
    }

    public Specification<E> addressLine3CyLike(String addressLinePattern) {
        return addressLineCyLike(AddressCyEntity_.addressLine3Cy, addressLinePattern);
    }

    public Specification<E> addressLineCyLike(
        SingularAttribute<? super E, String> attribute, String addressLinePattern) {

        return (root, query, builder) ->
            likeLowerCaseBothPredicate(root.get(attribute), builder, addressLinePattern);
    }
}
