package uk.gov.hmcts.opal.repository.jpa;

import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.search.AddressSearch;
import uk.gov.hmcts.opal.entity.AddressEntity;
import uk.gov.hmcts.opal.entity.AddressEntity_;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public abstract class AddressSpecs<E extends AddressEntity> extends EntitySpecs<E> {

    @SuppressWarnings("unchecked")
    public List<Optional<Specification<E>>> findByAddressCriteria(AddressSearch criteria) {
        return new ArrayList(Arrays.asList(
            notBlank(criteria.getName()).map(this::likeName),
            notBlank(criteria.getAddressLine()).map(this::likeAnyAddressLine),
            notBlank(criteria.getPostcode()).map(this::likePostcode)
        ));
    }

    public Specification<E> likeName(String name) {
        return (root, query, builder) -> builder.like(builder.lower(root.get(AddressEntity_.name)),
                                                      "%" + name.toLowerCase() + "%");
    }

    public Specification<E> likeAnyAddressLine(String addressLine) {
        String addressLinePattern = "%" + addressLine.toLowerCase() + "%";
        return Specification.anyOf(
            likeAddressLine1(addressLinePattern),
            likeAddressLine2(addressLinePattern),
            likeAddressLine3(addressLinePattern));
    }

    private Specification<E> likeAddressLine1(String addressLinePattern) {
        return (root, query, builder) -> builder.like(builder.lower(root.get(AddressEntity_.addressLine1)),
                                                      addressLinePattern);
    }

    private Specification<E> likeAddressLine2(String addressLinePattern) {
        return (root, query, builder) -> builder.like(builder.lower(root.get(AddressEntity_.addressLine2)),
                                                      addressLinePattern);
    }

    private Specification<E> likeAddressLine3(String addressLinePattern) {
        return (root, query, builder) -> builder.like(builder.lower(root.get(AddressEntity_.addressLine3)),
                                                      addressLinePattern);
    }

    public Specification<E> likePostcode(String postcode) {
        return (root, query, builder) -> builder.like(builder.lower(root.get(AddressEntity_.postcode)),
                                                      "%" + postcode.toLowerCase() + "%");
    }

}
