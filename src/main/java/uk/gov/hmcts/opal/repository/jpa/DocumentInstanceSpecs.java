package uk.gov.hmcts.opal.repository.jpa;

import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.search.DocumentInstanceSearchDto;
import uk.gov.hmcts.opal.entity.DocumentInstanceEntity;
import uk.gov.hmcts.opal.entity.DocumentInstanceEntity_;

public class DocumentInstanceSpecs extends EntitySpecs<DocumentInstanceEntity> {

    public Specification<DocumentInstanceEntity> findBySearchCriteria(DocumentInstanceSearchDto criteria) {
        return Specification.allOf(specificationList(
            notBlank(criteria.getDocumentInstanceId()).map(DocumentInstanceSpecs::equalsDocumentInstanceId)
        ));
    }

    public static Specification<DocumentInstanceEntity> equalsDocumentInstanceId(String documentInstanceId) {
        return (root, query, builder) -> builder.equal(root.get(DocumentInstanceEntity_.documentInstanceId),
                                                       documentInstanceId);
    }

}
