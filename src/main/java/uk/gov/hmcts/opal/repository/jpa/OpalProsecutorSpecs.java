package uk.gov.hmcts.opal.repository.jpa;

import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.entity.ProsecutorOpalEntity;
import uk.gov.hmcts.opal.entity.ProsecutorOpalEntity_;

public class OpalProsecutorSpecs extends EntitySpecs<ProsecutorOpalEntity> {

    public Specification<ProsecutorOpalEntity> referenceDataFilter(Optional<String> filter) {
        return Specification.allOf(specificationList(
            filter.filter(value -> !value.isBlank()).map(this::likeAnyProsecutor)
        ));
    }

    public static Specification<ProsecutorOpalEntity> equalsProsecutorCode(String prosecutorCode) {
        return (root, query, builder) -> builder.equal(root.get(ProsecutorOpalEntity_.prosecutorCode), prosecutorCode);
    }

    public static Optional<Specification<ProsecutorOpalEntity>> endDateGreaterThenEqualToDate(
        Optional<LocalDateTime> optDateTime) {

        return optDateTime.map(dateTime -> ((root, query, builder) -> builder.or(
            builder.isNull(root.get(ProsecutorOpalEntity_.endDate)),
            builder.greaterThanOrEqualTo(root.get(ProsecutorOpalEntity_.endDate), dateTime)
        )));
    }

    public Specification<ProsecutorOpalEntity> likeName(String name) {
        return (root, query, builder) -> likeWildcardPredicate(root.get(ProsecutorOpalEntity_.name), builder, name);
    }

    public Specification<ProsecutorOpalEntity> likeAddress1(String address) {
        return (root, query, builder) -> likeWildcardPredicate(
            root.get(ProsecutorOpalEntity_.addressLine1), builder, address);
    }

    public Specification<ProsecutorOpalEntity> likeAddress2(String address) {
        return (root, query, builder) -> likeWildcardPredicate(
            root.get(ProsecutorOpalEntity_.addressLine2), builder, address);
    }

    public Specification<ProsecutorOpalEntity> likeAddress3(String address) {
        return (root, query, builder) -> likeWildcardPredicate(
            root.get(ProsecutorOpalEntity_.addressLine3), builder, address);
    }

    public Specification<ProsecutorOpalEntity> likeAddress4(String address) {
        return (root, query, builder) -> likeWildcardPredicate(
            root.get(ProsecutorOpalEntity_.addressLine4), builder, address);
    }

    public Specification<ProsecutorOpalEntity> likeAddress5(String address) {
        return (root, query, builder) -> likeWildcardPredicate(
            root.get(ProsecutorOpalEntity_.addressLine5), builder, address);
    }

    public Specification<ProsecutorOpalEntity> likePostcode(String address) {
        return (root, query, builder) -> likeWildcardPredicate(
            root.get(ProsecutorOpalEntity_.postcode), builder, address);
    }

    public Specification<ProsecutorOpalEntity> likeAnyAddress(String filter) {
        return Specification.anyOf(
            likeAddress1(filter),
            likeAddress2(filter),
            likeAddress3(filter),
            likeAddress4(filter),
            likeAddress5(filter)
        );
    }

    public Specification<ProsecutorOpalEntity> likeAnyProsecutor(String filter) {
        return Specification.anyOf(
            likeName(filter),
            likeAnyAddress(filter),
            likePostcode(filter)
        );
    }
}
