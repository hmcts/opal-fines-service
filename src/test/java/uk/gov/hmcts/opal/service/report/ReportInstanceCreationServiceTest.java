package uk.gov.hmcts.opal.service.report;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.opal.testdata.ReportInstanceTestData.USER_ID;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import uk.gov.hmcts.opal.common.user.authorisation.model.BusinessUnitUser;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.generated.model.CreateReportInstanceRequestReports;
import uk.gov.hmcts.opal.generated.model.CreateReportInstanceResponseReports;
import uk.gov.hmcts.opal.service.UserStateService;

@ExtendWith(MockitoExtension.class)
class ReportInstanceCreationServiceTest {

    @Mock
    private UserStateService userStateService;
    @Mock
    private GenericReportService genericReportService;
    @Mock
    private UserState userState;
    @Mock
    private BusinessUnitUser businessUnitUser;

    @Test
    void createReportInstance_whenUserHasBusinessUnit_accessesGrs() {
        // Arrange
        ReportInstanceCreationService service =
            new ReportInstanceCreationService(userStateService, genericReportService);
        CreateReportInstanceRequestReports request = CreateReportInstanceRequestReports.builder()
            .reportId("cash_till")
            .businessUnitIds(List.of((short) 1))
            .reportParameters(Map.of())
            .build();
        CreateReportInstanceResponseReports response =
            CreateReportInstanceResponseReports.builder().reportInstanceId(123L).build();

        when(userStateService.getUserStateV1FromSecurityContext()).thenReturn(userState);
        when(userState.getUserId()).thenReturn(USER_ID);
        when(userState.getUserName()).thenReturn("test-user");
        when(userState.getBusinessUnitUser()).thenReturn(Set.of(businessUnitUser));
        when(businessUnitUser.getBusinessUnitId()).thenReturn((short) 1);
        when(genericReportService.addReportInstance(request, USER_ID, "test-user", true)).thenReturn(response);

        // Act
        CreateReportInstanceResponseReports actual = service.createReportInstance(request);

        // Assert
        assertThat(actual).isEqualTo(response);
        verify(genericReportService).addReportInstance(request, USER_ID, "test-user", true);
    }

    @Test
    void createReportInstance_whenUserLacksBusinessUnit_throwsAccessDenied() {
        // Arrange
        ReportInstanceCreationService service =
            new ReportInstanceCreationService(userStateService, genericReportService);
        CreateReportInstanceRequestReports request = CreateReportInstanceRequestReports.builder()
            .reportId("cash_till")
            .businessUnitIds(List.of((short) 1, (short) 2))
            .reportParameters(Map.of())
            .build();

        when(userStateService.getUserStateV1FromSecurityContext()).thenReturn(userState);
        when(userState.getBusinessUnitUser()).thenReturn(Set.of(businessUnitUser));
        when(businessUnitUser.getBusinessUnitId()).thenReturn((short) 1);

        // Act / Assert
        assertThatThrownBy(() -> service.createReportInstance(request))
            .isInstanceOf(AccessDeniedException.class)
            .hasMessage("You cannot generate reports for other business units");

        verifyNoInteractions(genericReportService);
    }
}
