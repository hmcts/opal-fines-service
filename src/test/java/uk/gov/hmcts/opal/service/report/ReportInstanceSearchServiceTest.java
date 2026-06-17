package uk.gov.hmcts.opal.service.report;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.opal.authorisation.model.FinesPermission.ACCOUNT_MAINTENANCE;
import static uk.gov.hmcts.opal.authorisation.model.FinesPermission.SEARCH_AND_VIEW_ACCOUNTS;
import static uk.gov.hmcts.opal.testdata.CommonTestData.businessUnitUserWithPermission;
import static uk.gov.hmcts.opal.testdata.ReportInstanceTestData.businessUnitUser;
import static uk.gov.hmcts.opal.testdata.ReportInstanceTestData.report;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.common.user.authorisation.model.BusinessUnitUser;
import uk.gov.hmcts.opal.entity.ReportEntity;
import uk.gov.hmcts.opal.repository.ReportRepository;
import uk.gov.hmcts.opal.service.UserStateService;

@ExtendWith(MockitoExtension.class)
class ReportInstanceSearchServiceTest {

    private static final String REPORT_ID = "R1";

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private UserStateService userStateService;

    @InjectMocks
    private ReportInstanceSearchService reportInstanceSearchService;

    private void mock_reportLookup(String reportId, ReportEntity reportEntity) {
        when(reportRepository.findById(reportId)).thenReturn(Optional.of(reportEntity));
    }

    private void mock_reportPermission(FinesPermission permission, boolean permitted) {
        when(userStateService.checkAnyBusinessUnitUserHasPermission(permission)).thenReturn(permitted);
    }

    @Nested
    class ThrowErrorIfReportIsProvidedButNotPermitted {

        @Test
        void whenReportIsPermitted_returnsReport_happyPath() {
            ReportEntity report = report(REPORT_ID, SEARCH_AND_VIEW_ACCOUNTS);
            mock_reportLookup(REPORT_ID, report);
            mock_reportPermission(SEARCH_AND_VIEW_ACCOUNTS, true);

            ReportEntity result = reportInstanceSearchService.throwErrorIfReportIsProvidedButNotPermitted(REPORT_ID);

            assertAll(
                () -> assertSame(report, result),
                () -> verify(reportRepository).findById(REPORT_ID)
            );
        }

        @Test
        void whenReportDoesNotExist_entityNotFoundIsThrown_sadPath() {
            when(reportRepository.findById(REPORT_ID)).thenReturn(Optional.empty());

            assertThrows(
                EntityNotFoundException.class,
                () -> reportInstanceSearchService.throwErrorIfReportIsProvidedButNotPermitted(REPORT_ID)
            );
        }

        @Test
        void whenReportPermissionIsMissing_accessDeniedIsThrown_sadPath() {
            ReportEntity report = report(REPORT_ID, ACCOUNT_MAINTENANCE);
            mock_reportLookup(REPORT_ID, report);
            mock_reportPermission(ACCOUNT_MAINTENANCE, false);

            AccessDeniedException exception = assertThrows(
                AccessDeniedException.class,
                () -> reportInstanceSearchService.throwErrorIfReportIsProvidedButNotPermitted(REPORT_ID)
            );

            assertAll(
                () -> assertThat(exception.getMessage())
                    .isEqualTo("User does not have permission for reportId: " + REPORT_ID),
                () -> verify(reportRepository).findById(REPORT_ID)
            );
        }

        @ParameterizedTest
        @NullSource
        @EmptySource
        void whenReportIdIsMissing_returnsNull_happyPath(String reportId) {
            ReportEntity result = reportInstanceSearchService.throwErrorIfReportIsProvidedButNotPermitted(reportId);

            assertAll(
                () -> assertThat(result).isNull(),
                () -> verify(reportRepository, never()).findById(anyString())
            );
        }
    }

    @Nested
    class ThrowErrorIfAnyBusinessUnitIsProvidedButNotPermitted {

        @Test
        void whenAnyBusinessUnitIsNotPermitted_accessDeniedIsThrown_sadPath() {
            AccessDeniedException exception = assertThrows(
                AccessDeniedException.class,
                () -> reportInstanceSearchService.throwErrorIfAnyBusinessUnitIsProvidedButNotPermitted(
                    List.of(10, 20)
                )
            );

            assertThat(exception.getMessage()).isEqualTo(
                "User does not have permission for one or more specified business units"
            );
        }

        @Test
        void whenAllBusinessUnitsArePermitted_noExceptionIsThrown_happyPath() {
            when(userStateService.getAllBusinessUnitUsersForCurrentUser()).thenReturn(List.of(
                businessUnitUserWithPermission("10", SEARCH_AND_VIEW_ACCOUNTS),
                businessUnitUserWithPermission("20", SEARCH_AND_VIEW_ACCOUNTS)
            ));

            assertDoesNotThrow(() ->
                reportInstanceSearchService.throwErrorIfAnyBusinessUnitIsProvidedButNotPermitted(List.of(10, 20))
            );
        }
    }

    @Nested
    class FindPermittedReportForBusinessUnits {

        @Test
        void whenBusinessUnitsMatchReportPermissions_returnsReportToBusinessUnitMap_happyPath() {
            ReportEntity searchReport = report("search", SEARCH_AND_VIEW_ACCOUNTS);
            ReportEntity maintenanceReport = report("maintain", ACCOUNT_MAINTENANCE);

            BusinessUnitUser buUser1 =
                businessUnitUser("BU1", (short) 10, SEARCH_AND_VIEW_ACCOUNTS, ACCOUNT_MAINTENANCE);
            BusinessUnitUser buUser2 = businessUnitUser("BU2", (short) 20, SEARCH_AND_VIEW_ACCOUNTS);

            when(userStateService.getBusinessUnitUsersForBusinessUnitIds(List.of(10L, 20L))).thenReturn(
                List.of(buUser1, buUser2)
            );

            Map<String, List<Long>> result = reportInstanceSearchService.findPermittedReportForBusinessUnits(
                List.of(searchReport, maintenanceReport),
                List.of(10L, 20L)
            );

            assertThat(result).containsExactlyInAnyOrderEntriesOf(Map.of(
                "search", List.of(10L, 20L),
                "maintain", List.of(10L)
            ));
        }

        @ParameterizedTest
        @NullSource
        @EmptySource
        void whenReportsAreMissing_returnsEmptyMap_happyPath(List<ReportEntity> reports) {
            assertThat(reportInstanceSearchService.findPermittedReportForBusinessUnits(reports, List.of(10L)))
                .isEmpty();
        }

        @ParameterizedTest
        @NullSource
        @EmptySource
        void whenBusinessUnitIdsAreMissing_returnsEmptyMap_happyPath(List<Long> businessUnitIds) {
            ReportEntity report = report("search", SEARCH_AND_VIEW_ACCOUNTS);

            assertThat(reportInstanceSearchService.findPermittedReportForBusinessUnits(
                List.of(report),
                businessUnitIds
            )).isEmpty();
        }
    }
}
