package uk.gov.hmcts.opal.repository.jpa;

import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.search.DocumentSearchDto;
import uk.gov.hmcts.opal.entity.DocumentEntity;
import uk.gov.hmcts.opal.entity.DocumentEntity_;

public class DocumentSpecs extends EntitySpecs<DocumentEntity> {

    public Specification<DocumentEntity> findBySearchCriteria(DocumentSearchDto criteria) {
        return Specification.allOf(specificationList(
            notBlank(criteria.getDocumentId()).map(DocumentSpecs::equalsDocumentId)
        ));
    }

    public static Specification<DocumentEntity> equalsDocumentId(String documentId) {
        return (root, query, builder) -> builder.equal(root.get(DocumentEntity_.documentId), documentId);
    }

}
