package uk.gov.hmcts.opal.service.report;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
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
import org.junit.jupiter.api.AfterEach;
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
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import uk.gov.hmcts.opal.common.spring.security.OpalJwtAuthenticationToken;
import uk.gov.hmcts.opal.common.user.authorisation.model.BusinessUnitUser;
import uk.gov.hmcts.opal.common.user.authorisation.model.Domain;
import uk.gov.hmcts.opal.common.user.authorisation.model.DomainBusinessUnitUsers;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserStateV2;
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

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private void setAuthenticatedUserWithPermissions(
        UserStateV2 userState,
        uk.gov.hmcts.opal.authorisation.model.FinesPermission... permissions
    ) {
        OpalJwtAuthenticationToken authToken = org.mockito.Mockito.mock(OpalJwtAuthenticationToken.class);
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authToken);
        SecurityContextHolder.setContext(securityContext);

        for (uk.gov.hmcts.opal.authorisation.model.FinesPermission permission : permissions) {
            when(authToken.hasPermission(permission.toUserPermission())).thenReturn(true);
        }

        if (userState != null) {
            when(userStateService.getUserStateFromSecurityContext()).thenReturn(userState);
        }
    }

    private UserStateV2 userStateWithBusinessUnitUsers(BusinessUnitUser... businessUnitUsers) {
        UserStateV2 userState = org.mockito.Mockito.mock(UserStateV2.class);
        DomainBusinessUnitUsers domainBusinessUnitUsers = org.mockito.Mockito.mock(DomainBusinessUnitUsers.class);
        when(userState.getDomainBusinessUnitUsers(Domain.FINES)).thenReturn(domainBusinessUnitUsers);
        when(domainBusinessUnitUsers.getBusinessUnitUsers()).thenReturn(List.of(businessUnitUsers));
        return userState;
    }

    @Nested
    class FindPermittedReports {

        @Test
        void whenReportsHaveMixedPermissions_returnsOnlyPermittedReports_happyPath() {
            ReportEntity permittedReport = report(REPORT_ID, SEARCH_AND_VIEW_ACCOUNTS);
            ReportEntity unpermittedReport = report("R2", ACCOUNT_MAINTENANCE);
            when(reportRepository.findAll()).thenReturn(List.of(permittedReport, unpermittedReport));
            setAuthenticatedUserWithPermissions(null, SEARCH_AND_VIEW_ACCOUNTS);

            List<ReportEntity> result = reportInstanceSearchService.findPermittedReports();

            assertAll(
                () -> assertIterableEquals(List.of(permittedReport), result),
                () -> verify(reportRepository).findAll()
            );
        }
    }

    @Nested
    class FindRequestedReport {

        @Test
        void whenReportIsPermitted_returnsReport_happyPath() {
            ReportEntity permittedReport = report(REPORT_ID, SEARCH_AND_VIEW_ACCOUNTS);

            ReportEntity result = reportInstanceSearchService.findRequestedReportElseThrowError(
                List.of(permittedReport),
                REPORT_ID
            );

            assertSame(permittedReport, result);
        }

        @Test
        void whenReportExistsButIsNotPermitted_accessDeniedIsThrown_sadPath() {
            when(reportRepository.existsById(REPORT_ID)).thenReturn(true);

            AccessDeniedException exception = assertThrows(
                AccessDeniedException.class,
                () -> reportInstanceSearchService.findRequestedReportElseThrowError(List.of(), REPORT_ID)
            );

            assertAll(
                () -> assertThat(exception.getMessage())
                    .isEqualTo("User does not have permission for reportId: " + REPORT_ID),
                () -> verify(reportRepository).existsById(REPORT_ID)
            );
        }

        @Test
        void whenReportDoesNotExist_entityNotFoundIsThrown_sadPath() {
            when(reportRepository.existsById(REPORT_ID)).thenReturn(false);

            assertThrows(
                EntityNotFoundException.class,
                () -> reportInstanceSearchService.findRequestedReportElseThrowError(List.of(), REPORT_ID)
            );
        }

        @ParameterizedTest
        @NullSource
        @EmptySource
        void whenReportIdIsMissing_returnsNull_happyPath(String reportId) {
            ReportEntity result = reportInstanceSearchService.findRequestedReportElseThrowError(List.of(), reportId);

            assertAll(
                () -> assertThat(result).isNull(),
                () -> verify(reportRepository, never()).existsById(anyString())
            );
        }
    }

    @Nested
    class FindPermittedBusinessUnitIds {

        @Test
        void whenCurrentUserHasBusinessUnits_returnsDistinctIds_happyPath() {
            setAuthenticatedUserWithPermissions(userStateWithBusinessUnitUsers(
                businessUnitUserWithPermission("10", SEARCH_AND_VIEW_ACCOUNTS),
                businessUnitUserWithPermission("20", SEARCH_AND_VIEW_ACCOUNTS),
                businessUnitUserWithPermission("10", SEARCH_AND_VIEW_ACCOUNTS)
            ));

            List<Long> result = reportInstanceSearchService.findPermittedBusinessUnitIds();

            assertThat(result).containsExactly(10L, 20L);
        }
    }

    @Nested
    class FindSelectedBusinessUnitIdsElseThrowError {

        @Test
        void whenRequestedBusinessUnitsArePermitted_returnsSelectedIds_happyPath() {
            List<Long> result = reportInstanceSearchService.findSelectedBusinessUnitIdsElseThrowError(
                List.of(10L, 20L, 30L),
                List.of(10, 30)
            );

            assertThat(result).containsExactly(10L, 30L);
        }

        @Test
        void whenRequestedBusinessUnitIsNotPermitted_accessDeniedIsThrown_sadPath() {
            AccessDeniedException exception = assertThrows(
                AccessDeniedException.class,
                () -> reportInstanceSearchService.findSelectedBusinessUnitIdsElseThrowError(
                    List.of(10L),
                    List.of(10, 20)
                )
            );

            assertThat(exception.getMessage()).isEqualTo(
                "User does not have permission for one or more specified business units"
            );
        }

        @ParameterizedTest
        @NullSource
        @EmptySource
        void whenRequestedBusinessUnitsAreMissing_returnsPermittedIds_happyPath(
            List<Integer> requestedBusinessUnitIds
        ) {
            List<Long> result = reportInstanceSearchService.findSelectedBusinessUnitIdsElseThrowError(
                List.of(10L, 20L),
                requestedBusinessUnitIds
            );

            assertThat(result).containsExactly(10L, 20L);
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

            setAuthenticatedUserWithPermissions(userStateWithBusinessUnitUsers(buUser1, buUser2));

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

        @Test
        void whenBusinessUnitUsersAreMissing_returnsEmptyMap_happyPath() {
            UserStateV2 userState = org.mockito.Mockito.mock(UserStateV2.class);
            DomainBusinessUnitUsers domainBusinessUnitUsers = org.mockito.Mockito.mock(DomainBusinessUnitUsers.class);
            when(userState.getDomainBusinessUnitUsers(Domain.FINES)).thenReturn(domainBusinessUnitUsers);
            when(domainBusinessUnitUsers.getBusinessUnitUsers()).thenReturn(null);
            setAuthenticatedUserWithPermissions(userState);

            assertThat(reportInstanceSearchService.findPermittedReportForBusinessUnits(
                List.of(report("search", SEARCH_AND_VIEW_ACCOUNTS)),
                List.of(10L)
            )).isEmpty();
        }
    }
}
