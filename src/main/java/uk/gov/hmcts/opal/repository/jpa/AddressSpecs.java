package uk.gov.hmcts.opal.repository.jpa;

import jakarta.persistence.metamodel.SingularAttribute;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.search.AddressSearch;
import uk.gov.hmcts.opal.entity.AddressEntity;
import uk.gov.hmcts.opal.entity.AddressEntity_;

public abstract class AddressSpecs<E extends AddressEntity> extends EntitySpecs<E> {

    public List<Optional<Specification<E>>> findByAddressCriteria(AddressSearch criteria) {
        return new ArrayList<>(List.of(
            notBlank(criteria.getName()).map(this::likeName),
            notBlank(criteria.getAddressLine()).map(this::likeAnyAddressLine),
            notBlank(criteria.getPostcode()).map(this::likePostcode)
        ));
    }

    public Specification<E> likeName(String name) {
        return (root, query, builder) -> likeWildcardPredicate(root.get(AddressEntity_.name), builder, name);
    }

    public Specification<E> likeAnyAddressLine(String addressLine) {
        String addressLinePattern = "%" + addressLine.toLowerCase() + "%";
        return Specification.anyOf(
            addressLine1Like(addressLinePattern),
            addressLine2Like(addressLinePattern),
            addressLine3Like(addressLinePattern));
    }

    public Specification<E> addressLine1Like(String addressLinePattern) {
        return addressLineLike(AddressEntity_.addressLine1, addressLinePattern);
    }

    public Specification<E> addressLine2Like(String addressLinePattern) {
        return addressLineLike(AddressEntity_.addressLine2, addressLinePattern);
    }

    public Specification<E> addressLine3Like(String addressLinePattern) {
        return addressLineLike(AddressEntity_.addressLine3, addressLinePattern);
    }

    public Specification<E> addressLineLike(SingularAttribute<? super E, String> attribute, String addressLinePattern) {
        return (root, query, builder) ->
            likeLowerCaseBothPredicate(root.get(attribute), builder, addressLinePattern);
    }

    public Specification<E> likePostcode(String postcode) {
        return (root, query, builder) ->
            likeWildcardPredicate(root.get(AddressEntity_.postcode), builder, postcode);
    }

}
