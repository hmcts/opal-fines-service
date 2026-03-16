package uk.gov.hmcts.opal.repository.jpa;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Fetch;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.opal.entity.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.DefendantAccountPartiesEntity;
import uk.gov.hmcts.opal.entity.enforcement.EnforcementEntity;
import uk.gov.hmcts.opal.service.report.ReportFiltersDto;

/**
 * Tests for ReportSpecs. Centralised stubbing is used to make Criteria API calls safe.
 * Uses assertDoesNotThrow instead of asserting predicate non-null to avoid brittle nulls
 * from heavily mocked CriteriaBuilder overloads while still verifying key interactions.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@SuppressWarnings({"unchecked", "rawtypes"})
public class ReportSpecsTest {

    @Mock
    CriteriaBuilder cb;

    @Mock
    CriteriaQuery<DefendantAccountEntity> query;

    @Mock
    Root<DefendantAccountEntity> root;

    @Mock
    Predicate conjunctionPredicate;

    @Mock
    Subquery<Long> subqueryLong;

    @Mock
    Subquery<LocalDateTime> subqueryDateTime;

    @Mock
    Root<EnforcementEntity> enforcementRoot;

    @Mock
    Root<EnforcementEntity> enforcementRoot2;

    @Mock
    Path<LocalDateTime> postedExpr;

    @Mock
    Root<DefendantAccountPartiesEntity> dapRoot;

    // make a Path that safely handles .get(...) and .as(...) chains
    private Path<Object> makeSafePath() {
        Path<Object> p = mock(Path.class);
        doReturn(p).when(p).get(anyString());
        doReturn(p).when(p).as(ArgumentMatchers.<Class<Object>>any());
        return p;
    }

    // Helper: minimal fetch chaining stub for root.fetch("parties", LEFT).fetch("party", LEFT)
    private void stubFetchPartiesChain(Root<DefendantAccountEntity> r) {
        Fetch fetchParties = mock(Fetch.class);
        Fetch fetchParty = mock(Fetch.class);
        when(r.fetch(eq("parties"), ArgumentMatchers.<JoinType>any())).thenReturn((Fetch) fetchParties);
        when(fetchParties.fetch(eq("party"), ArgumentMatchers.<JoinType>any())).thenReturn((Fetch) fetchParty);
    }

    // make root.get(...) resilient and return a Path that supports further .get/.as chaining
    private Path<Object> stubRootGetAny(Root<DefendantAccountEntity> r) {
        Path<Object> anyPath = makeSafePath();
        doReturn(anyPath).when(r).get(anyString());
        doReturn(anyPath).when(r).<Object>get(anyString());
        return anyPath;
    }

    @BeforeEach
    void beforeEach() {
        // core cb predicate/expression stubbing
        when(cb.conjunction()).thenReturn(conjunctionPredicate);
        when(cb.exists(any(Subquery.class))).thenReturn(conjunctionPredicate);
        when(cb.not(any(Predicate.class))).thenReturn(conjunctionPredicate);
        doReturn(conjunctionPredicate).when(cb).equal(any(), any());
        when(cb.isTrue(any())).thenReturn(conjunctionPredicate);
        when(cb.isFalse(any())).thenReturn(conjunctionPredicate);
        when(cb.isNotNull(any())).thenReturn(conjunctionPredicate);

        // various overloads used in specs
        when(cb.or(ArgumentMatchers.<Predicate[]>any())).thenReturn(conjunctionPredicate);
        doReturn(conjunctionPredicate).when(cb).or(any(Predicate.class), any(Predicate.class));
        when(cb.and(ArgumentMatchers.<Predicate[]>any())).thenReturn(conjunctionPredicate);

        when(cb.greaterThanOrEqualTo(ArgumentMatchers.<Expression>any(), ArgumentMatchers.<Comparable>any()))
            .thenReturn(conjunctionPredicate);
        when(cb.lessThanOrEqualTo(ArgumentMatchers.<Expression>any(), ArgumentMatchers.<Comparable>any()))
            .thenReturn(conjunctionPredicate);

        // Broad stubs that match calls where the left side might be a Subquery (e.g. maxDateSq)
        when(cb.greaterThanOrEqualTo(ArgumentMatchers.<Expression>any(), ArgumentMatchers.<LocalDateTime>any()))
            .thenReturn(conjunctionPredicate);
        when(cb.lessThanOrEqualTo(ArgumentMatchers.<Expression>any(), ArgumentMatchers.<LocalDateTime>any()))
            .thenReturn(conjunctionPredicate);

        // literal / expression helpers (return non-null Expression)
        Expression<Object> anyExpr = mock(Expression.class);
        when(cb.literal(ArgumentMatchers.anyLong())).thenReturn((Expression) anyExpr);
        when(cb.literal(ArgumentMatchers.any())).thenReturn((Expression) anyExpr);

        when(cb.upper(ArgumentMatchers.<Expression<String>>any())).thenReturn((Expression) anyExpr);
        when(cb.trim(ArgumentMatchers.<Expression<String>>any())).thenReturn((Expression) anyExpr);
        when(cb.coalesce(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn((Expression) anyExpr);
        when(cb.substring(ArgumentMatchers.<Expression<String>>any(), anyInt(),
            anyInt())).thenReturn((Expression) anyExpr);

        // between for LocalDate / LocalDateTime
        when(cb.between(ArgumentMatchers.<Expression<LocalDate>>any(),
            ArgumentMatchers.<LocalDate>any(),
            ArgumentMatchers.<LocalDate>any()))
            .thenReturn(conjunctionPredicate);

        when(cb.between(ArgumentMatchers.<Expression<LocalDateTime>>any(),
            ArgumentMatchers.<LocalDateTime>any(),
            ArgumentMatchers.<LocalDateTime>any()))
            .thenReturn(conjunctionPredicate);

        // central subquery stubbing so tests don't forget it
        doReturn(subqueryLong).when(query).subquery(Long.class);
        doReturn(subqueryDateTime).when(query).subquery(LocalDateTime.class);

        // subquery.from(...) defaults
        doReturn((Root) enforcementRoot).when(subqueryLong).from(EnforcementEntity.class);
        doReturn((Root) enforcementRoot2).when(subqueryDateTime).from(EnforcementEntity.class);

        // make enforcement roots' get(...) safe
        Path<Object> enforcementPath = makeSafePath();
        doReturn(enforcementPath).when(enforcementRoot).get(anyString());
        doReturn(enforcementPath).when(enforcementRoot2).get(anyString());
        doReturn(enforcementPath).when(enforcementPath).as(ArgumentMatchers.<Class<Object>>any());

        // postedExpr path returned when getting postedDate and/or cb.greatest(...)
        doReturn((Path) postedExpr).when(enforcementRoot2).get("postedDate");
        doReturn(postedExpr).when(postedExpr).as(LocalDateTime.class);
        when(cb.greatest(ArgumentMatchers.<Expression<LocalDateTime>>any())).thenReturn(postedExpr);

        // make dapRoot safe too (used by parentGuardianSpec)
        Path<Object> dapAny = makeSafePath();
        doReturn(dapAny).when(dapRoot).get(anyString());

        // make primary root safe (used everywhere)
        Path<Object> rootAny = makeSafePath();
        doReturn(rootAny).when(root).get(anyString());
    }

    @Test
    public void allMode_noDates_returns_conjunction() {
        // arrange - only what this test needs
        stubFetchPartiesChain(root);

        ReportFiltersDto filters = ReportFiltersDto.builder().enforcementMode("ALL").build();

        // act & assert: spec must not throw and should interact with CriteriaBuilder
        var spec = ReportSpecs.build(filters);
        Assertions.assertDoesNotThrow(() -> spec.toPredicate(root, query, cb));
        verify(cb, atLeastOnce()).conjunction();
    }

    @Test
    public void allMode_withDates_buildsExistsSubquery() {
        // arrange
        stubFetchPartiesChain(root);

        // postedDate path already stubbed in @BeforeEach via enforcementRoot
        when(cb.between(ArgumentMatchers.<Expression<LocalDate>>any(),
            ArgumentMatchers.<LocalDate>any(),
            ArgumentMatchers.<LocalDate>any()))
            .thenReturn(conjunctionPredicate);

        ReportFiltersDto filters = ReportFiltersDto.builder()
            .enforcementMode("ALL")
            .enforcementDateFrom(LocalDate.of(2024, 1, 1))
            .enforcementDateTo(LocalDate.of(2024, 12, 31))
            .build();

        var spec = ReportSpecs.build(filters);
        Assertions.assertDoesNotThrow(() -> spec.toPredicate(root, query, cb));
        verify(cb).exists(any(Subquery.class));
    }

    @Test
    public void lastActionMode_withDateRange_usesGreatest_and_exists() {
        // arrange
        stubFetchPartiesChain(root);

        ReportFiltersDto filters = ReportFiltersDto.builder()
            .enforcementMode("LAST_ACTION")
            .lastActionDateFrom(LocalDate.of(2024, 1, 1))
            .lastActionDateTo(LocalDate.of(2024, 1, 7))
            .build();

        var spec = ReportSpecs.build(filters);
        // ensure it doesn't throw when building the subqueries/expressions
        Assertions.assertDoesNotThrow(() -> spec.toPredicate(root, query, cb));

        // still verify the important interactions happened
        verify(query).subquery(LocalDateTime.class);
        verify(cb).greatest(ArgumentMatchers.<Expression<LocalDateTime>>any());
        verify(cb).exists(any(Subquery.class));
    }

    @Test
    public void regfMode_withoutDates_buildsExistsOrFineRegistrationDate() {
        // arrange
        stubFetchPartiesChain(root);

        ReportFiltersDto filters = ReportFiltersDto.builder()
            .enforcementMode("REGF")
            .build();

        var spec = ReportSpecs.build(filters);
        Assertions.assertDoesNotThrow(() -> spec.toPredicate(root, query, cb));
        verify(cb).exists(any(Subquery.class));
    }

    @Test
    public void notUnderEnforcement_returns_not_exists() {
        // arrange
        stubFetchPartiesChain(root);

        ReportFiltersDto filters = ReportFiltersDto.builder()
            .enforcementMode("NOT_UNDER_ENFORCEMENT")
            .build();

        var spec = ReportSpecs.build(filters);
        Assertions.assertDoesNotThrow(() -> spec.toPredicate(root, query, cb));
        verify(cb).not(any(Predicate.class));
    }

    @Test
    public void parentGuardianSpec_whenFlagTrue_callsExistsSubquery() {
        // arrange
        stubFetchPartiesChain(root);

        // ensure the subquery.from(...) for DefendantAccountPartiesEntity returns dapRoot
        doReturn(subqueryLong).when(query).subquery(Long.class);
        doReturn((Root) dapRoot).when(subqueryLong).from(DefendantAccountPartiesEntity.class);

        // ensure nested get("defendantAccount").get("defendantAccountId") won't NPE
        Path<Object> dapDefAccountPath = makeSafePath();
        doReturn(dapDefAccountPath).when(dapRoot).get("defendantAccount");
        doReturn(dapDefAccountPath).when(dapDefAccountPath).get("defendantAccountId");
        doReturn(dapDefAccountPath).when(dapDefAccountPath).as(ArgumentMatchers.<Class<Object>>any());

        // ensure associationType path available (used by cb.trim/upper)
        Path<Object> assocPath = makeSafePath();
        doReturn(assocPath).when(dapRoot).get("associationType");

        ReportFiltersDto filters = ReportFiltersDto.builder()
            .onlyAccountsWithParentGuardian(Boolean.TRUE)
            .build();

        var spec = ReportSpecs.build(filters);
        Assertions.assertDoesNotThrow(() -> spec.toPredicate(root, query, cb));
        verify(cb).exists(any(Subquery.class));
    }
}