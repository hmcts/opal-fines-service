package uk.gov.hmcts.opal.repository.jpa;

import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.NotesSearchDto;
import uk.gov.hmcts.opal.entity.NoteEntity;
import uk.gov.hmcts.opal.entity.NoteEntity_;

import java.time.LocalDateTime;

public class NoteSpecs extends EntitySpecs<NoteEntity> {

    public Specification<NoteEntity> findBySearchCriteria(NotesSearchDto criteria) {
        return Specification.allOf(specificationList(
            notBlank(criteria.getAssociatedType()).map(NoteSpecs::equalsAssociatedType),
            notBlank(criteria.getAssociatedId()).map(NoteSpecs::equalsAssociatedId),
            notBlank(criteria.getNoteType()).map(NoteSpecs::equalsNoteType),
            notBlank(criteria.getPostedBy()).map(NoteSpecs::equalsPostedBy),
            notNullLocalDateTime(criteria.getPostedDate()).map(NoteSpecs::equalsPostedDate)
        ));
    }

    public static Specification<NoteEntity> equalsAssociatedType(String associatedType) {
        return (root, query, builder) -> builder.equal(builder.lower(root.get(NoteEntity_.associatedRecordType)),
                             associatedType.toLowerCase());
    }

    public static Specification<NoteEntity> equalsAssociatedId(String associatedId) {
        return (root, query, builder) -> builder.equal(root.get(NoteEntity_.associatedRecordId), associatedId);
    }

    public static Specification<NoteEntity> equalsNoteType(String noteType) {
        return (root, query, builder) -> builder.equal(builder.lower(root.get(NoteEntity_.noteType)),
                             noteType.toLowerCase());
    }

    public static Specification<NoteEntity> equalsPostedBy(String postedBy) {
        return (root, query, builder) -> builder.equal(builder.lower(root.get(NoteEntity_.postedBy)),
                             postedBy.toLowerCase());
    }

    public static Specification<NoteEntity> equalsPostedDate(LocalDateTime posted) {
        return (root, query, builder) -> builder.equal(root.get(NoteEntity_.postedDate), posted);
    }


}
