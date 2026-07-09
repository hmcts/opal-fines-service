package uk.gov.hmcts.opal.repository.jpa;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.hibernate.query.criteria.JpaExpression;
import org.hibernate.query.criteria.JpaPath;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.search.AmendmentSearchDto;
import uk.gov.hmcts.opal.entity.AssociatedRecordType;
import uk.gov.hmcts.opal.entity.amendment.AmendmentEntity;
import uk.gov.hmcts.opal.entity.amendment.AmendmentEntity_;

class AmendmentSpecsTest {

    private final AmendmentSpecs specs = new AmendmentSpecs();

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findBySearchCriteria_associatedRecordType_usesEnum() {
        AmendmentSearchDto criteria = AmendmentSearchDto.builder()
            .associatedRecordType("defendant_accounts")
            .build();

        final Specification<AmendmentEntity> spec = specs.findBySearchCriteria(criteria);

        Root<AmendmentEntity> root = mock(Root.class);
        final CriteriaQuery<AmendmentEntity> query = mock(CriteriaQuery.class);
        CriteriaBuilder criteriaBuilder = mock(CriteriaBuilder.class);
        JpaPath<AssociatedRecordType> path = mock(JpaPath.class);
        JpaExpression<String> castExpression = mock(JpaExpression.class);
        Predicate predicate = mock(Predicate.class);

        when(root.get(AmendmentEntity_.associatedRecordType)).thenReturn(path);
        doReturn(castExpression).when(path).cast(String.class);
        when(criteriaBuilder.equal(any(), any())).thenReturn(predicate);

        spec.toPredicate(root, query, criteriaBuilder);
        verify(criteriaBuilder).equal(castExpression, AssociatedRecordType.DEFENDANT_ACCOUNTS.getLabel());
    }

    @Test
    void findBySearchCriteria_blankAssociatedRecordType_isIgnored() {
        AmendmentSearchDto criteria = AmendmentSearchDto.builder()
            .amendmentId("1")
            .associatedRecordType(" ")
            .build();

        assertDoesNotThrow(() -> specs.findBySearchCriteria(criteria));
    }

    @Test
    void findBySearchCriteria_invalidAssociatedRecordType_throws() {
        AmendmentSearchDto criteria = AmendmentSearchDto.builder()
            .associatedRecordType("not_a_real_type")
            .build();

        assertThrows(IllegalArgumentException.class, () -> specs.findBySearchCriteria(criteria));
    }
}
