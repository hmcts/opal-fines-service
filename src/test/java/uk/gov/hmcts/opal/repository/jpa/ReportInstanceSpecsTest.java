package uk.gov.hmcts.opal.repository.jpa;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static uk.gov.hmcts.opal.repository.jpa.ReportInstanceSpecs.build;
import static uk.gov.hmcts.opal.repository.jpa.ReportInstanceSpecs.createdTimestampFrom;
import static uk.gov.hmcts.opal.repository.jpa.ReportInstanceSpecs.createdTimestampTo;
import static uk.gov.hmcts.opal.repository.jpa.ReportInstanceSpecs.hasAnyBusinessUnitIn;
import static uk.gov.hmcts.opal.repository.jpa.ReportInstanceSpecs.reportIdEquals;
import static uk.gov.hmcts.opal.repository.jpa.ReportInstanceSpecs.requestedByEquals;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.opal.entity.ReportInstanceEntity;

class ReportInstanceSpecsTest {

    private static Stream<Arguments> emptyBusinessUnitIds() {
        return Stream.of(
            Arguments.of((List<Short>) null),
            Arguments.of(List.of()),
            Arguments.of(Collections.<Short>singletonList(null))
        );
    }

    @Test
    void codeCoverageTest() {
        assertAll(
            () -> assertNotNull(build(
                LocalDate.now(),
                LocalDate.now(),
                123L,
                "report-id",
                List.of((short) 10)
            )),
            () -> assertNotNull(createdTimestampFrom(LocalDateTime.now())),
            () -> assertNotNull(createdTimestampTo(LocalDateTime.now())),
            () -> assertNotNull(requestedByEquals(123L)),
            () -> assertNotNull(reportIdEquals("report-id")),
            () -> assertNotNull(hasAnyBusinessUnitIn(List.of((short) 10)))
        );
    }

    @Nested
    class HasAnyBusinessUnitIn {

        @SuppressWarnings("unchecked")
        Root<ReportInstanceEntity> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);

        @ParameterizedTest
        @MethodSource("uk.gov.hmcts.opal.repository.jpa.ReportInstanceSpecsTest#emptyBusinessUnitIds")
        void whenBusinessUnitIdsAreMissing_returnsNullPredicate_happyPath(List<Short> businessUnitIds) {
            assertNull(hasAnyBusinessUnitIn(businessUnitIds).toPredicate(root, query, cb));
        }
    }
}
