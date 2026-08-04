package uk.gov.hmcts.opal.repository.jpa;

import org.hibernate.query.criteria.JpaExpression;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.entity.AssociatedRecordType;
import uk.gov.hmcts.opal.entity.NoteEntity;
import uk.gov.hmcts.opal.entity.NoteEntity_;
import uk.gov.hmcts.opal.entity.NoteType;

public class NoteSpecs extends EntitySpecs<NoteEntity> {
    public static Specification<NoteEntity> equalsAssociatedRecordType(
        AssociatedRecordType associatedRecordType) {
        return (root, query, builder) -> builder.equal(
            ((JpaExpression<?>) root.get(NoteEntity_.associatedRecordType)).cast(String.class),
            associatedRecordType.getLabel());
    }

    public static Specification<NoteEntity> equalsAssociatedRecordId(String associatedRecordId) {
        return (root, query, builder) -> builder.equal(
            root.get(NoteEntity_.associatedRecordId), associatedRecordId);
    }

    public static Specification<NoteEntity> equalsNoteType(NoteType noteType) {
        return (root, query, builder) -> builder.equal(
            ((JpaExpression<?>) root.get(NoteEntity_.noteType)).cast(String.class),
            noteType.name());
    }
}
