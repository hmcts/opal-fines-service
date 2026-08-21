package uk.gov.hmcts.opal.repository.jpa;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.metamodel.SingularAttribute;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.search.DefendantDto;
import uk.gov.hmcts.opal.entity.search.SearchDefendantAccount.BasicEntity;

class SearchDefendantAccountSpecsTest {

    @Test
    void matchPersonNamesAndAliases_aliasSurnameUsesStartsWithWhenExactMatchIsFalse() {
        CriteriaMocks criteria = criteriaMocks();
        SearchBasicEntitySpecs specs = new SearchBasicEntitySpecs();

        Specification<BasicEntity> specification = specs.matchPersonNamesAndAliases(DefendantDto.builder()
            .includeAliases(true)
            .organisation(false)
            .surname("sent")
            .exactMatchSurname(false)
            .build(), true);

        specification.toPredicate(criteria.root, criteria.query, criteria.cb);

        verify(criteria.cb, atLeastOnce()).like(criteria.lowerAliasSurname, "sent%");
        verify(criteria.cb, times(5)).function(eq("regexp_replace"), eq(String.class), any(Expression.class),
            any(Expression.class), any(Expression.class));
        verify(criteria.cb, never()).like(criteria.lowerAliasSurname, "%sent%");
        verify(criteria.cb, never()).like(criteria.lowerAliasSurname, "%sent");
    }

    @Test
    void matchPersonNamesAndAliases_aliasSurnameUsesEqualsWhenExactMatchIsTrue() {
        CriteriaMocks criteria = criteriaMocks();
        SearchBasicEntitySpecs specs = new SearchBasicEntitySpecs();

        Specification<BasicEntity> specification = specs.matchPersonNamesAndAliases(DefendantDto.builder()
            .includeAliases(true)
            .organisation(false)
            .surname("sent")
            .exactMatchSurname(true)
            .build(), true);

        specification.toPredicate(criteria.root, criteria.query, criteria.cb);

        verify(criteria.cb, atLeastOnce()).equal(criteria.lowerAliasSurname, "sent");
        verify(criteria.cb, times(5)).function(eq("regexp_replace"), eq(String.class), any(Expression.class),
            any(Expression.class), any(Expression.class));
        verify(criteria.cb, never()).like(criteria.lowerAliasSurname, "%sent%");
        verify(criteria.cb, never()).like(criteria.lowerAliasSurname, "%sent");
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private CriteriaMocks criteriaMocks() {
        Root<BasicEntity> root = mock(Root.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Path aliasPath = mock(Path.class);
        Expression<String> literal = mock(Expression.class);
        Expression<String> regexpAliasSurname = mock(Expression.class);
        Expression<String> strippedAliasSurname = mock(Expression.class);
        Expression<String> lowerAliasSurname = mock(Expression.class);
        Predicate predicate = mock(Predicate.class);

        when(root.get((SingularAttribute) isNull(SingularAttribute.class))).thenReturn(aliasPath);
        when(cb.literal(anyString())).thenReturn(literal);
        when(cb.function(eq("regexp_replace"), eq(String.class), any(Expression.class), any(Expression.class),
            any(Expression.class))).thenReturn(regexpAliasSurname);
        when(cb.function(eq("translate"), eq(String.class), any(Expression.class), any(Expression.class),
            any(Expression.class))).thenReturn(strippedAliasSurname);
        when(cb.lower(strippedAliasSurname)).thenReturn(lowerAliasSurname);
        when(cb.like(any(Expression.class), anyString())).thenReturn(predicate);
        when(cb.equal(any(Expression.class), anyString())).thenReturn(predicate);
        when(cb.isFalse(any(Expression.class))).thenReturn(predicate);
        when(cb.or(any(Predicate[].class))).thenReturn(predicate);
        when(cb.and(any(Predicate[].class))).thenReturn(predicate);

        return new CriteriaMocks(root, mock(CriteriaQuery.class), cb, lowerAliasSurname);
    }

    private record CriteriaMocks(
        Root<BasicEntity> root,
        CriteriaQuery<?> query,
        CriteriaBuilder cb,
        Expression<String> lowerAliasSurname) {
    }
}
