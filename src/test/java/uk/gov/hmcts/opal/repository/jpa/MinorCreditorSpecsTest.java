package uk.gov.hmcts.opal.repository.jpa;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.opal.dto.MinorCreditorSearch;
import uk.gov.hmcts.opal.entity.minorcreditor.MinorCreditorEntity;
import uk.gov.hmcts.opal.entity.minorcreditor.MinorCreditorEntity_;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MinorCreditorSpecsTest {

    private final MinorCreditorSpecs specs = new MinorCreditorSpecs();

    @Test
    void findBySearchCriteria_nullCriteria_throwsBadRequest() {
        ResponseStatusException ex = assertThrows(
            ResponseStatusException.class,
            () -> specs.findBySearchCriteria(null)
        );
        assertTrue(ex.getReason().contains("Search criteria must be provided"));
    }

    @Test
    void findBySearchCriteria_noFilters_throwsBadRequest() {
        MinorCreditorSearch criteria = mock(MinorCreditorSearch.class);
        when(criteria.getBusinessUnitIds()).thenReturn(null);
        when(criteria.getAccountNumber()).thenReturn(null);
        when(criteria.getCreditor()).thenReturn(null);

        ResponseStatusException ex = assertThrows(
            ResponseStatusException.class,
            () -> specs.findBySearchCriteria(criteria)  // combineAnd should throw
        );
        assertTrue(ex.getReason().contains("at least one filter"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void businessUnitIds_only_buildsInPredicateWithShorts_andDropsNulls() {
        MinorCreditorSearch criteria = mock(MinorCreditorSearch.class);
        when(criteria.getBusinessUnitIds()).thenReturn(Arrays.asList(1, null, 2));
        when(criteria.getAccountNumber()).thenReturn(null);
        when(criteria.getCreditor()).thenReturn(null);

        Specification<MinorCreditorEntity> spec = specs.findBySearchCriteria(criteria);

        Root<MinorCreditorEntity> root = mock(Root.class);
        CriteriaQuery<MinorCreditorEntity> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Path<Short> buPath = mock(Path.class);
        Predicate inPredicate = mock(Predicate.class);

        doReturn(buPath).when(root).get(MinorCreditorEntity_.BUSINESS_UNIT_ID);
        when(buPath.in(any(Collection.class))).thenReturn(inPredicate);
        when(cb.and(any(), any())).thenAnswer(inv -> inv.getArgument(1));

        Predicate result = spec.toPredicate(root, query, cb);

        assertSame(inPredicate, result);
        ArgumentCaptor<Collection<Short>> captor = ArgumentCaptor.forClass(Collection.class);
        verify(buPath).in(captor.capture());
        Collection<Short> passed = captor.getValue();
        assertEquals(2, passed.size());
        assertTrue(passed.contains((short) 1));
        assertTrue(passed.contains((short) 2));
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void accountNumber_only_callsHasTextStripAndLike_withStrippedPrefix() {
        MinorCreditorSearch criteria = mock(MinorCreditorSearch.class);
        when(criteria.getBusinessUnitIds()).thenReturn(null);
        when(criteria.getAccountNumber()).thenReturn("12345678A"); // should become "12345678"
        when(criteria.getCreditor()).thenReturn(null);

        Root<MinorCreditorEntity> root = mock(Root.class);
        CriteriaQuery<MinorCreditorEntity> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Predicate likePredicate = mock(Predicate.class);

        when(cb.and(any(), any())).thenAnswer(inv -> inv.getArgument(1));

        try (MockedStatic<SpecificationUtils> utils = mockStatic(SpecificationUtils.class)) {

            utils.when(() -> SpecificationUtils.hasText("12345678A")).thenReturn(true);
            utils.when(() -> SpecificationUtils.stripCheckLetter("12345678A")).thenReturn("12345678");


            Specification<MinorCreditorEntity> spec = new MinorCreditorSpecs().findBySearchCriteria(criteria);

            utils.when(() -> SpecificationUtils.likeStartsWithNormalized(
                    root, cb, MinorCreditorEntity_.ACCOUNT_NUMBER, "12345678"))
                .thenReturn(likePredicate);

            Predicate out = spec.toPredicate(root, query, cb);
            assertSame(likePredicate, out);

            utils.verify(() -> SpecificationUtils.hasText("12345678A"));
            utils.verify(() -> SpecificationUtils.stripCheckLetter("12345678A"));
            utils.verify(() -> SpecificationUtils.likeStartsWithNormalized(
                root, cb, MinorCreditorEntity_.ACCOUNT_NUMBER, "12345678"));
            utils.verifyNoMoreInteractions();
        }
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void accountNumber_blank_hasTextFalse_resultsInBadRequestWhenNoOtherFilters() {
        MinorCreditorSearch criteria = mock(MinorCreditorSearch.class);
        when(criteria.getBusinessUnitIds()).thenReturn(null);
        when(criteria.getAccountNumber()).thenReturn("   ");
        when(criteria.getCreditor()).thenReturn(null);

        try (MockedStatic<SpecificationUtils> utils = mockStatic(SpecificationUtils.class)) {
            utils.when(() -> SpecificationUtils.hasText("   ")).thenReturn(false);

            ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> specs.findBySearchCriteria(criteria)
            );
            Assertions.assertNotNull(ex.getReason());
            assertTrue(ex.getReason().contains("at least one filter"));

            utils.verify(() -> SpecificationUtils.hasText("   "));
            utils.verifyNoMoreInteractions();
        }
    }
}
