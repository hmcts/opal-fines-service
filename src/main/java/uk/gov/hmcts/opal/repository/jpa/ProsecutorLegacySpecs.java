package uk.gov.hmcts.opal.repository.jpa;

import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.entity.ProsecutorLegacyEntity;
import uk.gov.hmcts.opal.entity.ProsecutorLegacyEntity_;

public class ProsecutorLegacySpecs extends EntitySpecs<ProsecutorLegacyEntity> {

    public Specification<ProsecutorLegacyEntity> referenceDataFilter(Optional<String> filter) {
        return Specification.allOf(specificationList(
            filter.filter(value -> !value.isBlank()).map(this::likeAnyProsecutor)
        ));
    }

    public static Optional<Specification<ProsecutorLegacyEntity>> endDateGreaterThenEqualToDate(
        Optional<LocalDateTime> optDateTime) {

        return optDateTime.map(dateTime -> ((root, query, builder) -> builder.or(
            builder.isNull(root.get(ProsecutorLegacyEntity_.endDate)),
            builder.greaterThanOrEqualTo(root.get(ProsecutorLegacyEntity_.endDate), dateTime)
        )));
    }

    public Specification<ProsecutorLegacyEntity> likeName(String name) {
        return (root, query, builder) ->
            likeWildcardPredicate(root.get(ProsecutorLegacyEntity_.name), builder, name);
    }

    public Specification<ProsecutorLegacyEntity> likeAddress1(String address) {
        return (root, query, builder) -> likeWildcardPredicate(
            root.get(ProsecutorLegacyEntity_.addressLine1), builder, address);
    }

    public Specification<ProsecutorLegacyEntity> likeAddress2(String address) {
        return (root, query, builder) -> likeWildcardPredicate(
            root.get(ProsecutorLegacyEntity_.addressLine2), builder, address);
    }

    public Specification<ProsecutorLegacyEntity> likeAddress3(String address) {
        return (root, query, builder) -> likeWildcardPredicate(
            root.get(ProsecutorLegacyEntity_.addressLine3), builder, address);
    }

    public Specification<ProsecutorLegacyEntity> likeAddress4(String address) {
        return (root, query, builder) -> likeWildcardPredicate(
            root.get(ProsecutorLegacyEntity_.addressLine4), builder, address);
    }

    public Specification<ProsecutorLegacyEntity> likeAddress5(String address) {
        return (root, query, builder) -> likeWildcardPredicate(
            root.get(ProsecutorLegacyEntity_.addressLine5), builder, address);
    }

    public Specification<ProsecutorLegacyEntity> likePostcode(String address) {
        return (root, query, builder) -> likeWildcardPredicate(
            root.get(ProsecutorLegacyEntity_.postcode), builder, address);
    }

    public Specification<ProsecutorLegacyEntity> likeAnyAddress(String filter) {
        return Specification.anyOf(
            likeAddress1(filter),
            likeAddress2(filter),
            likeAddress3(filter),
            likeAddress4(filter),
            likeAddress5(filter)
        );
    }

    public Specification<ProsecutorLegacyEntity> likeAnyProsecutor(String filter) {
        return Specification.anyOf(
            likeName(filter),
            likeAnyAddress(filter),
            likePostcode(filter)
        );
    }
}
