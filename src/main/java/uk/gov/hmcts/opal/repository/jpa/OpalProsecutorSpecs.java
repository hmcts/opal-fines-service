package uk.gov.hmcts.opal.repository.jpa;

import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.entity.OpalProsecutorEntity;
import uk.gov.hmcts.opal.entity.OpalProsecutorEntity_;

public class OpalProsecutorSpecs extends EntitySpecs<OpalProsecutorEntity> {

    public Specification<OpalProsecutorEntity> referenceDataFilter(Optional<String> filter) {
        return Specification.allOf(specificationList(
            filter.filter(value -> !value.isBlank()).map(this::likeAnyProsecutor)
        ));
    }

    public static Specification<OpalProsecutorEntity> equalsProsecutorCode(String prosecutorCode) {
        return (root, query, builder) -> builder.equal(root.get(OpalProsecutorEntity_.prosecutorCode), prosecutorCode);
    }

    public static Optional<Specification<OpalProsecutorEntity>> endDateGreaterThenEqualToDate(
        Optional<LocalDateTime> optDateTime) {

        return optDateTime.map(dateTime -> ((root, query, builder) -> builder.or(
            builder.isNull(root.get(OpalProsecutorEntity_.endDate)),
            builder.greaterThanOrEqualTo(root.get(OpalProsecutorEntity_.endDate), dateTime)
        )));
    }

    public Specification<OpalProsecutorEntity> likeName(String name) {
        return (root, query, builder) -> likeWildcardPredicate(root.get(OpalProsecutorEntity_.name), builder, name);
    }

    public Specification<OpalProsecutorEntity> likeAddress1(String address) {
        return (root, query, builder) -> likeWildcardPredicate(
            root.get(OpalProsecutorEntity_.addressLine1), builder, address);
    }

    public Specification<OpalProsecutorEntity> likeAddress2(String address) {
        return (root, query, builder) -> likeWildcardPredicate(
            root.get(OpalProsecutorEntity_.addressLine2), builder, address);
    }

    public Specification<OpalProsecutorEntity> likeAddress3(String address) {
        return (root, query, builder) -> likeWildcardPredicate(
            root.get(OpalProsecutorEntity_.addressLine3), builder, address);
    }

    public Specification<OpalProsecutorEntity> likeAddress4(String address) {
        return (root, query, builder) -> likeWildcardPredicate(
            root.get(OpalProsecutorEntity_.addressLine4), builder, address);
    }

    public Specification<OpalProsecutorEntity> likeAddress5(String address) {
        return (root, query, builder) -> likeWildcardPredicate(
            root.get(OpalProsecutorEntity_.addressLine5), builder, address);
    }

    public Specification<OpalProsecutorEntity> likePostcode(String address) {
        return (root, query, builder) -> likeWildcardPredicate(
            root.get(OpalProsecutorEntity_.postcode), builder, address);
    }

    public Specification<OpalProsecutorEntity> likeAnyAddress(String filter) {
        return Specification.anyOf(
            likeAddress1(filter),
            likeAddress2(filter),
            likeAddress3(filter),
            likeAddress4(filter),
            likeAddress5(filter)
        );
    }

    public Specification<OpalProsecutorEntity> likeAnyProsecutor(String filter) {
        return Specification.anyOf(
            likeName(filter),
            likeAnyAddress(filter),
            likePostcode(filter)
        );
    }
}
