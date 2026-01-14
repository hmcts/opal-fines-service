package uk.gov.hmcts.opal.repository.jpa;

import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.search.AddressCySearch;
import uk.gov.hmcts.opal.entity.AddressCyEntity;
import uk.gov.hmcts.opal.entity.AddressCyEntity_;

import java.util.List;
import java.util.Optional;


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
            likeAddressLine1Cy(addressLinePattern),
            likeAddressLine2Cy(addressLinePattern),
            likeAddressLine3Cy(addressLinePattern));
    }

    private Specification<E> likeAddressLine1Cy(String addressLinePattern) {
        return (root, query, builder) ->
            likeLowerCaseBothPredicate(root.get(AddressCyEntity_.addressLine1Cy), builder, addressLinePattern);
    }

    private Specification<E> likeAddressLine2Cy(String addressLinePattern) {
        return (root, query, builder) ->
            likeLowerCaseBothPredicate(root.get(AddressCyEntity_.addressLine2Cy), builder, addressLinePattern);
    }

    private Specification<E> likeAddressLine3Cy(String addressLinePattern) {
        return (root, query, builder) ->
            likeLowerCaseBothPredicate(root.get(AddressCyEntity_.addressLine3Cy), builder, addressLinePattern);
    }
}
