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
            notBlank(criteria.getName()).map(this::equalsName),
            notBlank(criteria.getAddressLine()).map(this::equalsAddressLine),
            notBlank(criteria.getPostcode()).map(this::equalsPostcode)
        ));
    }

    public Specification<E> equalsName(String name) {
        return (root, query, builder) -> builder.equal(root.get(AddressEntity_.name), name);
    }

    public Specification<E> equalsAddressLine(String addressLine) {
        return (root, query, builder) -> builder.equal(root.get(AddressEntity_.addressLine1), addressLine);
    }

    public Specification<E> equalsPostcode(String postcode) {
        return (root, query, builder) -> builder.equal(root.get(AddressEntity_.postcode), postcode);
    }

}
