package uk.gov.hmcts.opal.service.report;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.opal.entity.report.ReportInstanceGenerationStatus.REQUESTED;
import static uk.gov.hmcts.opal.service.report.ReportId.CASH_TILL;

import jakarta.persistence.EntityNotFoundException;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.entity.ReportEntity;
import uk.gov.hmcts.opal.entity.ReportInstanceEntity;
import uk.gov.hmcts.opal.entity.TillEntity;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;
import uk.gov.hmcts.opal.exception.EntityNotSavedException;
import uk.gov.hmcts.opal.repository.ReportInstanceRepository;
import uk.gov.hmcts.opal.repository.ReportRepository;
import uk.gov.hmcts.opal.repository.TillRepository;
import uk.gov.hmcts.opal.service.UserStateService;

@ExtendWith(MockitoExtension.class)
class PreAllocatedCashTillServiceTest {

    private static final Long TILL_ID = 321L;
    private static final Long REPORT_INSTANCE_ID = 1234L;
    private static final Long USER_ID = 987L;
    private static final String USER_NAME = "report.user";
    private static final LocalDateTime REQUESTED_AT = LocalDateTime.of(2026, 6, 23, 10, 15, 30);

    private final Clock clock = Clock.fixed(Instant.parse("2026-06-23T10:15:30Z"), ZoneOffset.UTC);
    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    @Mock
    private ReportInstanceRepository reportInstanceRepository;
    @Mock
    private ReportRepository reportRepository;
    @Mock
    private TillRepository tillRepository;
    @Mock
    private UserStateService userStateService;
    @Mock
    private GenericReportService genericReportService;

    private PreAllocatedCashTillService service;

    @BeforeEach
    void setUp() {
        service = new PreAllocatedCashTillService(clock, objectMapper, reportInstanceRepository, reportRepository,
            tillRepository, userStateService, genericReportService);
    }

    @Test
    void createPreAllocatedReportInstance_savesInstanceAndGeneratesReport() throws Exception {
        ReportEntity report = cashTillReport();
        TillEntity till = tillWithBusinessUnit();
        UserState userState = UserState.builder().userId(USER_ID).userName(USER_NAME).build();
        ArgumentCaptor<ReportInstanceEntity> reportInstanceCaptor = ArgumentCaptor.forClass(ReportInstanceEntity.class);

        when(tillRepository.findById(TILL_ID)).thenReturn(Optional.of(till));
        when(reportRepository.findById(CASH_TILL.getReportId())).thenReturn(Optional.of(report));
        when(userStateService.getUserStateV1FromSecurityContext()).thenReturn(userState);
        when(reportInstanceRepository.save(any(ReportInstanceEntity.class))).thenAnswer(invocation -> {
            ReportInstanceEntity reportInstance = invocation.getArgument(0);
            reportInstance.setReportInstanceId(REPORT_INSTANCE_ID);
            return reportInstance;
        });

        Long reportInstanceId = service.createPreAllocatedReportInstance(TILL_ID);

        assertThat(reportInstanceId).isEqualTo(REPORT_INSTANCE_ID);
        verify(reportInstanceRepository).save(reportInstanceCaptor.capture());
        ReportInstanceEntity savedReportInstance = reportInstanceCaptor.getValue();
        assertThat(savedReportInstance.getReport()).isSameAs(report);
        assertThat(savedReportInstance.getBusinessUnit()).containsExactly(77);
        assertThat(savedReportInstance.getRequestedBy()).isEqualTo(USER_ID);
        assertThat(savedReportInstance.getRequestedByName()).isEqualTo(USER_NAME);
        assertThat(savedReportInstance.getRequestedAt()).isEqualTo(REQUESTED_AT);
        assertThat(savedReportInstance.getGenerationStatus()).isEqualTo(REQUESTED);
        assertThat(savedReportInstance.getReportName()).isEqualTo("Cash till report - Pre-allocated (17)");

        Map parameters = objectMapper.readValue(savedReportInstance.getReportParameters(), Map.class);
        assertThat(((Number) parameters.get("till_id")).longValue()).isEqualTo(TILL_ID);
        assertThat(parameters.get("allocated_report")).isEqualTo(false);
        verify(genericReportService).generateReportInstanceContent(REPORT_INSTANCE_ID);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(longs = {0L, -1L})
    void createPreAllocatedReportInstance_whenTillIdIsMissingOrInvalid_throwsException(Long tillId) {
        assertThatThrownBy(() -> service.createPreAllocatedReportInstance(tillId))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Cash Till report till_id is required");

        verifyNoInteractions(tillRepository, reportRepository, reportInstanceRepository, genericReportService);
    }

    @Test
    void createPreAllocatedReportInstance_whenTillCannotBeFound_throwsException() {
        when(tillRepository.findById(TILL_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createPreAllocatedReportInstance(TILL_ID))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessage("Cash Till report till not found for till_id 321");

        verifyNoInteractions(reportRepository, reportInstanceRepository, genericReportService);
    }

    @Test
    void createPreAllocatedReportInstance_whenReportCannotBeFound_throwsException() {
        when(tillRepository.findById(TILL_ID)).thenReturn(Optional.of(tillWithBusinessUnit()));
        when(reportRepository.findById(CASH_TILL.getReportId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createPreAllocatedReportInstance(TILL_ID))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessage("Report not found with id: cash_till");

        verifyNoInteractions(reportInstanceRepository, genericReportService);
    }

    @Test
    void createPreAllocatedReportInstance_whenTillHasNoBusinessUnit_throwsException() {
        when(tillRepository.findById(TILL_ID)).thenReturn(Optional.of(TillEntity.builder().tillId(TILL_ID).build()));
        when(reportRepository.findById(CASH_TILL.getReportId())).thenReturn(Optional.of(cashTillReport()));
        when(userStateService.getUserStateV1FromSecurityContext())
            .thenReturn(UserState.builder().userId(USER_ID).userName(USER_NAME).build());

        assertThatThrownBy(() -> service.createPreAllocatedReportInstance(TILL_ID))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessage("Cash Till report business unit not found for till_id 321");

        verifyNoInteractions(reportInstanceRepository, genericReportService);
    }

    @Test
    void createPreAllocatedReportInstance_whenSaveFails_doesNotGenerateReport() {
        when(tillRepository.findById(TILL_ID)).thenReturn(Optional.of(tillWithBusinessUnit()));
        when(reportRepository.findById(CASH_TILL.getReportId())).thenReturn(Optional.of(cashTillReport()));
        when(userStateService.getUserStateV1FromSecurityContext())
            .thenReturn(UserState.builder().userId(USER_ID).userName(USER_NAME).build());
        when(reportInstanceRepository.save(any(ReportInstanceEntity.class))).thenThrow(new RuntimeException("db down"));

        assertThatThrownBy(() -> service.createPreAllocatedReportInstance(TILL_ID))
            .isInstanceOf(EntityNotSavedException.class)
            .hasMessage("Unable to save report instance");

        verify(genericReportService, never()).generateReportInstanceContent(any());
    }

    private static ReportEntity cashTillReport() {
        return ReportEntity.builder().reportId(CASH_TILL.getReportId()).build();
    }

    private static TillEntity tillWithBusinessUnit() {
        return TillEntity.builder()
            .tillId(TILL_ID)
            .tillNumber((short) 17)
            .businessUnit(BusinessUnitEntity.builder().businessUnitId((short) 77).build())
            .build();
    }
}
