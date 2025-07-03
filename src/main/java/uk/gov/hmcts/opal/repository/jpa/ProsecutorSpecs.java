package uk.gov.hmcts.opal.repository.jpa;

import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.entity.ProsecutorEntity;
import uk.gov.hmcts.opal.entity.ProsecutorEntity_;

import java.time.LocalDateTime;
import java.util.Optional;

public class ProsecutorSpecs extends EntitySpecs<ProsecutorEntity> {

    public Specification<ProsecutorEntity> referenceDataFilter(Optional<String> filter) {
        return Specification.allOf(specificationList(
            filter.filter(s -> !s.isBlank()).map(this::likeAnyProsecutor)
        ));
    }

    public static Specification<ProsecutorEntity> equalsProsecutorId(String prosecutorId) {
        return (root, query, builder) -> builder.equal(root.get(ProsecutorEntity_.prosecutorId), prosecutorId);
    }

    public static Specification<ProsecutorEntity> equalsProsecutorCode(String prosecutorCode) {
        return (root, query, builder) -> builder.equal(root.get(ProsecutorEntity_.prosecutorCode), prosecutorCode);
    }

    public static Optional<Specification<ProsecutorEntity>> endDateGreaterThenEqualToDate(
        Optional<LocalDateTime> optDateTime) {

        return optDateTime.map(dateTime -> ((root, query, builder) -> builder.or(
            builder.isNull(root.get(ProsecutorEntity_.endDate)),
            builder.greaterThanOrEqualTo(root.get(ProsecutorEntity_.endDate), dateTime)
        )));
    }

    public Specification<ProsecutorEntity> likeName(String name) {
        return (root, query, builder) -> likeWildcardPredicate(root.get(ProsecutorEntity_.name), builder, name);
    }

    public Specification<ProsecutorEntity> likeAddress1(String address) {
        return (root, query, builder) -> likeWildcardPredicate(
            root.get(ProsecutorEntity_.addressLine1), builder, address);
    }

    public Specification<ProsecutorEntity> likeAddress2(String address) {
        return (root, query, builder) -> likeWildcardPredicate(
            root.get(ProsecutorEntity_.addressLine2), builder, address);
    }

    public Specification<ProsecutorEntity> likeAddress3(String address) {
        return (root, query, builder) -> likeWildcardPredicate(
            root.get(ProsecutorEntity_.addressLine3), builder, address);
    }

    public Specification<ProsecutorEntity> likeAddress4(String address) {
        return (root, query, builder) -> likeWildcardPredicate(
            root.get(ProsecutorEntity_.addressLine4), builder, address);
    }

    public Specification<ProsecutorEntity> likeAddress5(String address) {
        return (root, query, builder) -> likeWildcardPredicate(
            root.get(ProsecutorEntity_.addressLine5), builder, address);
    }

    public Specification<ProsecutorEntity> likePostcode(String address) {
        return (root, query, builder) -> likeWildcardPredicate(
            root.get(ProsecutorEntity_.postcode), builder, address);
    }

    public Specification<ProsecutorEntity> likeAnyAddress(String filter) {
        return Specification.anyOf(
            likeAddress1(filter),
            likeAddress2(filter),
            likeAddress3(filter),
            likeAddress4(filter),
            likeAddress5(filter)
        );
    }

    public Specification<ProsecutorEntity> likeAnyProsecutor(String filter) {
        return Specification.anyOf(
            likeName(filter),
            likeAnyAddress(filter),
            likePostcode(filter)
        );
    }

}
