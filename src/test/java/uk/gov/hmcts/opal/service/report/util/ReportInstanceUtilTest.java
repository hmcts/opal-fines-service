package uk.gov.hmcts.opal.service.report.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.opal.authorisation.model.FinesPermission.ACCOUNT_MAINTENANCE;
import static uk.gov.hmcts.opal.authorisation.model.FinesPermission.SEARCH_AND_VIEW_ACCOUNTS;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import uk.gov.hmcts.opal.common.user.authorisation.model.BusinessUnitUser;
import uk.gov.hmcts.opal.entity.ReportEntity;
import uk.gov.hmcts.opal.repository.ReportRepository;
import uk.gov.hmcts.opal.service.UserStateService;
import uk.gov.hmcts.opal.testdata.ReportInstanceTestData;

@ExtendWith(MockitoExtension.class)
class ReportInstanceUtilTest {

    private static final String REPORT_ID = "R1";

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private UserStateService userStateService;

    @Test
    void throwErrorIfReportIsProvidedButNotPermitted_returnsReportWhenPermitted() {
        ReportEntity report = ReportInstanceTestData.report(REPORT_ID, SEARCH_AND_VIEW_ACCOUNTS);
        when(reportRepository.findById(REPORT_ID)).thenReturn(Optional.of(report));
        when(userStateService.checkAnyBusinessUnitUserHasPermission(SEARCH_AND_VIEW_ACCOUNTS)).thenReturn(true);

        ReportEntity result = ReportInstanceUtil.throwErrorIfReportIsProvidedButNotPermitted(
            reportRepository,
            userStateService,
            REPORT_ID
        );

        assertSame(report, result);
    }

    @Test
    void throwErrorIfReportIsProvidedButNotPermitted_throwsWhenReportNotFound() {
        when(reportRepository.findById(REPORT_ID)).thenReturn(Optional.empty());

        assertThrows(
            EntityNotFoundException.class,
            () -> ReportInstanceUtil.throwErrorIfReportIsProvidedButNotPermitted(
                reportRepository,
                userStateService,
                REPORT_ID
            )
        );
    }

    @Test
    void throwErrorIfReportIsProvidedButNotPermitted_throwsWhenPermissionMissing() {
        ReportEntity report = ReportInstanceTestData.report(REPORT_ID, ACCOUNT_MAINTENANCE);
        when(reportRepository.findById(REPORT_ID)).thenReturn(Optional.of(report));
        when(userStateService.checkAnyBusinessUnitUserHasPermission(ACCOUNT_MAINTENANCE)).thenReturn(false);

        AccessDeniedException exception = assertThrows(
            AccessDeniedException.class,
            () -> ReportInstanceUtil.throwErrorIfReportIsProvidedButNotPermitted(
                reportRepository,
                userStateService,
                REPORT_ID
            )
        );

        assertThat(exception.getMessage()).isEqualTo("User does not have permission for reportId: " + REPORT_ID);
    }

    @Test
    void throwErrorIfReportIsProvidedButNotPermitted_returnsNullWhenReportIdBlank() {
        ReportEntity result = ReportInstanceUtil.throwErrorIfReportIsProvidedButNotPermitted(
            reportRepository,
            userStateService,
            " "
        );

        assertThat(result).isNull();
        verify(reportRepository, never()).findById(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void throwErrorIfAnyBusinessUnitIsProvidedButNotPermitted_throwsWhenAnyUnpermittedBusinessUnitExists() {
        when(userStateService.isBusinessUnitPermittedForCurrentUser((short) 10)).thenReturn(true);
        when(userStateService.isBusinessUnitPermittedForCurrentUser((short) 20)).thenReturn(false);

        AccessDeniedException exception = assertThrows(
            AccessDeniedException.class,
            () -> ReportInstanceUtil.throwErrorIfAnyBusinessUnitIsProvidedButNotPermitted(
                userStateService,
                List.of(10, 20)
            )
        );

        assertThat(exception.getMessage()).isEqualTo(
            "User does not have permission for one or more specified business units"
        );
    }

    @Test
    void throwErrorIfAnyBusinessUnitIsProvidedButNotPermitted_allowsWhenAllBusinessUnitsArePermitted() {
        when(userStateService.isBusinessUnitPermittedForCurrentUser((short) 10)).thenReturn(true);
        when(userStateService.isBusinessUnitPermittedForCurrentUser((short) 20)).thenReturn(true);

        assertDoesNotThrow(() ->
            ReportInstanceUtil.throwErrorIfAnyBusinessUnitIsProvidedButNotPermitted(
                userStateService,
                List.of(10, 20)
            )
        );
    }

    @Test
    void findPermittedReportForBusinessUnits_returnsReportToBusinessUnitMap() {
        ReportEntity searchReport = ReportInstanceTestData.report("search", SEARCH_AND_VIEW_ACCOUNTS);
        ReportEntity maintenanceReport = ReportInstanceTestData.report("maintain", ACCOUNT_MAINTENANCE);

        BusinessUnitUser firstBusinessUnitUser = BusinessUnitUser.builder()
            .businessUnitUserId("BU1")
            .businessUnitId((short) 10)
            .permissions(Set.of(
                SEARCH_AND_VIEW_ACCOUNTS.toUserPermission(),
                ACCOUNT_MAINTENANCE.toUserPermission()
            ))
            .build();

        BusinessUnitUser secondBusinessUnitUser = BusinessUnitUser.builder()
            .businessUnitUserId("BU2")
            .businessUnitId((short) 20)
            .permissions(Set.of(SEARCH_AND_VIEW_ACCOUNTS.toUserPermission()))
            .build();

        when(userStateService.getBusinessUnitUsersForBusinessUnitIds(List.of(10L, 20L)))
            .thenReturn(List.of(firstBusinessUnitUser, secondBusinessUnitUser));

        Map<String, List<Long>> result = ReportInstanceUtil.findPermittedReportForBusinessUnits(
            userStateService,
            List.of(searchReport, maintenanceReport),
            List.of(10L, 20L)
        );

        assertThat(result).containsExactlyInAnyOrderEntriesOf(Map.of(
            "search", List.of(10L, 20L),
            "maintain", List.of(10L)
        ));
    }

    @Test
    void findPermittedReportForBusinessUnits_returnsEmptyMapWhenInputsMissing() {
        assertThat(ReportInstanceUtil.findPermittedReportForBusinessUnits(userStateService, List.of(), List.of(10L)))
            .isEmpty();
    }
}
