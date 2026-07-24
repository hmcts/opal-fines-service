package uk.gov.hmcts.opal.service.report;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.opal.service.report.ReportId.CASH_TILL;

import jakarta.persistence.EntityNotFoundException;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.entity.TillEntity;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;
import uk.gov.hmcts.opal.generated.model.CreateReportInstanceRequestReports;
import uk.gov.hmcts.opal.generated.model.CreateReportInstanceResponseReports;
import uk.gov.hmcts.opal.repository.TillRepository;

@ExtendWith(MockitoExtension.class)
class PreAllocatedCashTillServiceTest {

    private static final Long TILL_ID = 321L;
    private static final Long REPORT_INSTANCE_ID = 1234L;
    private static final Long USER_ID = 987L;
    private static final String USER_NAME = "report.user";

    @Mock
    private TillRepository tillRepository;
    @Mock
    private GenericReportService genericReportService;

    private PreAllocatedCashTillService service;

    @BeforeEach
    void setUp() {
        service = new PreAllocatedCashTillService(tillRepository, genericReportService);
    }

    @Test
    void createPreAllocatedReportInstance_buildsRequestAndDelegatesToGrsSynchronously() {
        TillEntity till = tillWithBusinessUnit();
        CreateReportInstanceResponseReports response =
            CreateReportInstanceResponseReports.builder().reportInstanceId(REPORT_INSTANCE_ID).build();
        ArgumentCaptor<CreateReportInstanceRequestReports> requestCaptor =
            ArgumentCaptor.forClass(CreateReportInstanceRequestReports.class);

        when(tillRepository.findById(TILL_ID)).thenReturn(Optional.of(till));
        when(genericReportService.addReportInstance(
            any(CreateReportInstanceRequestReports.class), eq(USER_ID), eq(USER_NAME), eq(false))).thenReturn(response);

        Long reportInstanceId = service.createPreAllocatedReportInstance(TILL_ID, USER_ID, USER_NAME);

        assertThat(reportInstanceId).isEqualTo(REPORT_INSTANCE_ID);
        verify(genericReportService).addReportInstance(requestCaptor.capture(), eq(USER_ID), eq(USER_NAME), eq(false));
        CreateReportInstanceRequestReports request = requestCaptor.getValue();
        assertThat(request.getReportId()).isEqualTo(CASH_TILL.getReportId());
        assertThat(request.getBusinessUnitIds()).containsExactly((short) 77);
        assertThat(request.getReportName()).isEqualTo("Cash till report - Pre-allocated (17)");
        assertThat(request.getReportParameters()).isEqualTo(Map.of(
            "till_id", TILL_ID,
            "allocated_report", false
        ));
    }

    @Test
    void createPreAllocatedReportInstance_whenTillIdIsMissingOrInvalid_throwsException() {
        assertThatThrownBy(() -> service.createPreAllocatedReportInstance(null, USER_ID, USER_NAME))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Cash Till report till_id is required");

        assertThatThrownBy(() -> service.createPreAllocatedReportInstance(0L, USER_ID, USER_NAME))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Cash Till report till_id is required");

        assertThatThrownBy(() -> service.createPreAllocatedReportInstance(-1L, USER_ID, USER_NAME))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Cash Till report till_id is required");

        verifyNoInteractions(tillRepository, genericReportService);
    }

    @Test
    void createPreAllocatedReportInstance_whenTillCannotBeFound_throwsException() {
        when(tillRepository.findById(TILL_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createPreAllocatedReportInstance(TILL_ID, USER_ID, USER_NAME))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessage("Cash Till report till not found for till_id 321");

        verifyNoInteractions(genericReportService);
    }

    @Test
    void createPreAllocatedReportInstance_whenTillHasNoBusinessUnit_throwsException() {
        when(tillRepository.findById(TILL_ID)).thenReturn(Optional.of(TillEntity.builder().tillId(TILL_ID).build()));

        assertThatThrownBy(() -> service.createPreAllocatedReportInstance(TILL_ID, USER_ID, USER_NAME))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessage("Cash Till report business unit not found for till_id 321");

        verifyNoInteractions(genericReportService);
    }

    @Test
    void createPreAllocatedReportInstance_whenGrsFails_doesNotContinue() {
        when(tillRepository.findById(TILL_ID)).thenReturn(Optional.of(tillWithBusinessUnit()));
        when(genericReportService.addReportInstance(any(CreateReportInstanceRequestReports.class), eq(USER_ID),
            eq(USER_NAME), eq(false))).thenThrow(new RuntimeException("db down"));

        assertThatThrownBy(() -> service.createPreAllocatedReportInstance(TILL_ID, USER_ID, USER_NAME))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("db down");

        verify(genericReportService, never()).generateReportInstanceContent(any());
    }

    private static TillEntity tillWithBusinessUnit() {
        return TillEntity.builder()
            .tillId(TILL_ID)
            .tillNumber((short) 17)
            .businessUnit(BusinessUnitEntity.builder().businessUnitId((short) 77).build())
            .build();
    }
}
