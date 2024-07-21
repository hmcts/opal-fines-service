package uk.gov.hmcts.opal.repository.jpa;

import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.search.ResultDocumentSearchDto;
import uk.gov.hmcts.opal.entity.ResultDocumentEntity;
import uk.gov.hmcts.opal.entity.ResultDocumentEntity_;

public class ResultDocumentSpecs extends EntitySpecs<ResultDocumentEntity> {

    public Specification<ResultDocumentEntity> findBySearchCriteria(ResultDocumentSearchDto criteria) {
        return Specification.allOf(specificationList(
            notBlank(criteria.getResultDocumentId()).map(ResultDocumentSpecs::equalsResultDocumentId)
        ));
    }

    public static Specification<ResultDocumentEntity> equalsResultDocumentId(String resultDocumentId) {
        return (root, query, builder) -> builder.equal(root.get(ResultDocumentEntity_.resultDocumentId),
                                                       resultDocumentId);
    }

}
