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
import org.junit.jupiter.api.BeforeEach;
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
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
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

    @Mock
    private UserStateV2 userStateV2;

    @Mock
    private DomainBusinessUnitUsers domainBusinessUnitUsers;

    @Mock
    private OpalJwtAuthenticationToken authToken;

    @InjectMocks
    private ReportInstanceSearchService reportInstanceSearchService;

    @BeforeEach
    void setUp() {
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authToken);
        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private void setAuthenticatedUserWithPermissions(FinesPermission... permissions) {
        for (FinesPermission permission : permissions) {
            when(authToken.hasPermission(permission.toCommonPermission())).thenReturn(true);
        }
    }

    private void setBusinessUnitUsers(BusinessUnitUser... businessUnitUsers) {
        when(userStateService.getUserStateFromSecurityContext()).thenReturn(userStateV2);
        when(userStateV2.getDomainBusinessUnitUsers(Domain.FINES)).thenReturn(domainBusinessUnitUsers);
        when(domainBusinessUnitUsers.getBusinessUnitUsers()).thenReturn(List.of(businessUnitUsers));
    }

    private void setNoBusinessUnitUsers() {
        when(userStateService.getUserStateFromSecurityContext()).thenReturn(userStateV2);
        when(userStateV2.getDomainBusinessUnitUsers(Domain.FINES)).thenReturn(domainBusinessUnitUsers);
        when(domainBusinessUnitUsers.getBusinessUnitUsers()).thenReturn(null);
    }

    @Nested
    class FindPermittedReports {

        @Test
        void whenReportsHaveMixedPermissions_returnsOnlyPermittedReports_happyPath() {
            ReportEntity permittedReport = report(REPORT_ID, SEARCH_AND_VIEW_ACCOUNTS);
            ReportEntity unpermittedReport = report("R2", ACCOUNT_MAINTENANCE);
            when(reportRepository.findAll()).thenReturn(List.of(permittedReport, unpermittedReport));
            setAuthenticatedUserWithPermissions(SEARCH_AND_VIEW_ACCOUNTS);

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
            when(reportRepository.findById(REPORT_ID)).thenReturn(java.util.Optional.of(permittedReport));
            setAuthenticatedUserWithPermissions(SEARCH_AND_VIEW_ACCOUNTS);

            ReportEntity result = reportInstanceSearchService.findRequestedReportElseThrowError(REPORT_ID);

            assertSame(permittedReport, result);
        }

        @Test
        void whenReportExistsButIsNotPermitted_accessDeniedIsThrown_sadPath() {
            when(reportRepository.findById(REPORT_ID)).thenReturn(
                java.util.Optional.of(report(REPORT_ID, SEARCH_AND_VIEW_ACCOUNTS))
            );
            setAuthenticatedUserWithPermissions();

            AccessDeniedException exception = assertThrows(
                AccessDeniedException.class,
                () -> reportInstanceSearchService.findRequestedReportElseThrowError(REPORT_ID)
            );

            assertAll(
                () -> assertThat(exception.getMessage())
                    .isEqualTo("User does not have permission for reportId: " + REPORT_ID),
                () -> verify(reportRepository).findById(REPORT_ID)
            );
        }

        @Test
        void whenReportDoesNotExist_entityNotFoundIsThrown_sadPath() {
            when(reportRepository.findById(REPORT_ID)).thenReturn(java.util.Optional.empty());

            assertThrows(
                EntityNotFoundException.class,
                () -> reportInstanceSearchService.findRequestedReportElseThrowError(REPORT_ID)
            );
        }

        @ParameterizedTest
        @NullSource
        @EmptySource
        void whenReportIdIsMissing_returnsNull_happyPath(String reportId) {
            ReportEntity result = reportInstanceSearchService.findRequestedReportElseThrowError(reportId);

            assertAll(
                () -> assertThat(result).isNull(),
                () -> verify(reportRepository, never()).findById(anyString())
            );
        }
    }

    @Nested
    class ValidateBusinessUnitIds {

        @Test
        void whenCurrentUserHasBusinessUnits_returnsDistinctIds_happyPath() {
            setBusinessUnitUsers(
                businessUnitUserWithPermission("10", SEARCH_AND_VIEW_ACCOUNTS),
                businessUnitUserWithPermission("20", SEARCH_AND_VIEW_ACCOUNTS),
                businessUnitUserWithPermission("10", SEARCH_AND_VIEW_ACCOUNTS)
            );

            List<Short> result = reportInstanceSearchService.validateBusinessUnitIds(null);

            assertThat(result).containsExactly((short) 10, (short) 20);
        }

        @Test
        void whenRequestedBusinessUnitsArePermitted_returnsSelectedIds_happyPath() {
            setBusinessUnitUsers(
                businessUnitUserWithPermission("10", SEARCH_AND_VIEW_ACCOUNTS),
                businessUnitUserWithPermission("20", SEARCH_AND_VIEW_ACCOUNTS),
                businessUnitUserWithPermission("30", SEARCH_AND_VIEW_ACCOUNTS)
            );

            List<Short> result = reportInstanceSearchService.validateBusinessUnitIds(List.of((short) 10, (short) 30));

            assertThat(result).containsExactly((short) 10, (short) 30);
        }

        @Test
        void whenRequestedBusinessUnitIsNotPermitted_accessDeniedIsThrown_sadPath() {
            setBusinessUnitUsers(
                businessUnitUserWithPermission("10", SEARCH_AND_VIEW_ACCOUNTS)
            );

            AccessDeniedException exception = assertThrows(
                AccessDeniedException.class,
                () -> reportInstanceSearchService.validateBusinessUnitIds(List.of((short) 10, (short) 20))
            );

            assertThat(exception.getMessage()).isEqualTo("User does not have permission for business unit: 20");
        }

        @ParameterizedTest
        @NullSource
        @EmptySource
        void whenRequestedBusinessUnitsAreMissing_returnsPermittedIds_happyPath(
            List<Short> requestedBusinessUnitIds
        ) {
            setBusinessUnitUsers(
                businessUnitUserWithPermission("10", SEARCH_AND_VIEW_ACCOUNTS),
                businessUnitUserWithPermission("20", SEARCH_AND_VIEW_ACCOUNTS)
            );

            List<Short> result = reportInstanceSearchService.validateBusinessUnitIds(requestedBusinessUnitIds);

            assertThat(result).containsExactly((short) 10, (short) 20);
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

            setBusinessUnitUsers(buUser1, buUser2);

            Map<String, List<Short>> result = reportInstanceSearchService.findPermittedReportForBusinessUnits(
                List.of(searchReport, maintenanceReport),
                List.of((short) 10, (short) 20)
            );

            assertThat(result).containsExactlyInAnyOrderEntriesOf(Map.of(
                "search", List.of((short) 10, (short) 20),
                "maintain", List.of((short) 10)
            ));
        }

        @ParameterizedTest
        @NullSource
        @EmptySource
        void whenReportsAreMissing_returnsEmptyMap_happyPath(List<ReportEntity> reports) {
            assertThat(reportInstanceSearchService.findPermittedReportForBusinessUnits(reports, List.of((short) 10)))
                .isEmpty();
        }

        @ParameterizedTest
        @NullSource
        @EmptySource
        void whenBusinessUnitIdsAreMissing_returnsEmptyMap_happyPath(List<Short> businessUnitIds) {
            ReportEntity report = report("search", SEARCH_AND_VIEW_ACCOUNTS);

            assertThat(reportInstanceSearchService.findPermittedReportForBusinessUnits(
                List.of(report),
                businessUnitIds
            )).isEmpty();
        }

        @Test
        void whenBusinessUnitUsersAreMissing_returnsEmptyMap_happyPath() {
            setNoBusinessUnitUsers();

            assertThat(reportInstanceSearchService.findPermittedReportForBusinessUnits(
                List.of(report("search", SEARCH_AND_VIEW_ACCOUNTS)),
                List.of((short) 10)
            )).isEmpty();
        }
    }
}
