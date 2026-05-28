package uk.gov.hmcts.opal.service.report;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.opal.entity.report.ReportInstanceGenerationStatus.ERROR;
import static uk.gov.hmcts.opal.entity.report.ReportInstanceGenerationStatus.READY;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import uk.gov.hmcts.opal.common.user.authorisation.model.BusinessUnitUser;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.entity.ReportEntity;
import uk.gov.hmcts.opal.entity.ReportInstanceEntity;
import uk.gov.hmcts.opal.exception.ReportGenerationException;
import uk.gov.hmcts.opal.exception.ReportNotFoundException;
import uk.gov.hmcts.opal.exception.UnprocessableException;
import uk.gov.hmcts.opal.generated.model.CreateReportInstanceRequestReports;
import uk.gov.hmcts.opal.generated.model.CreateReportInstanceResponseReports;
import uk.gov.hmcts.opal.mapper.ReportInstanceMapper;
import uk.gov.hmcts.opal.repository.ReportInstanceRepository;
import uk.gov.hmcts.opal.repository.ReportRepository;
import uk.gov.hmcts.opal.service.UserStateService;
import uk.gov.hmcts.opal.service.blobstore.ReportBlobStore;
import uk.gov.hmcts.opal.service.messaging.ReportQueuePublisherImpl;

@ExtendWith(MockitoExtension.class)
class GenericReportServiceTest {

    public static final String LOCATION = "location";

    @InjectMocks
    private GenericReportService genericReportService;

    @Mock
    ReportInstanceRepository reportInstanceRepository;

    @Mock
    ReportRepository reportRepository;

    @Mock
    ReportBlobStore reportBlobStore;

    @Mock
    ReportRegistry reportRegistry;

    @SuppressWarnings("rawtypes")
    @Mock
    ReportInterface reportInterfaceImplementation;

    @Mock
    Clock clock;

    ReportInstanceEntity reportInstance;

    @Mock
    ReportEntity reportEntity;

    @Mock
    TestData reportData;

    @Mock
    ObjectMapper mapper;

    @Mock
    UserStateService userStateService;

    @Mock
    UserState userState;

    @Mock
    BusinessUnitUser businessUnitUser1;

    @Mock
    BusinessUnitUser businessUnitUser2;

    @Mock
    ReportInstanceMapper reportInstanceMapper;

    @Mock
    CreateReportInstanceResponseReports reportInstanceResponse;

    @Mock
    Map<String, Object> reportParameters;

    @Mock
    ReportQueuePublisherImpl reportQueuePublisher;

    @Mock
    ReportParameterValidator reportParameterValidator;

    String reportId;

    Instant now;

    @BeforeEach
    void setUp()  {
        reportInstance = new ReportInstanceEntity();
        reportId = String.valueOf(UUID.randomUUID());
        reportInstance.setReport(reportEntity);

        now = Instant.parse("2026-01-01T10:00:00Z");
        Mockito.lenient().when(clock.instant()).thenReturn(now);
        Mockito.lenient().when(clock.getZone()).thenReturn(ZoneOffset.UTC);
    }

    @Test
    void generateReportInstanceContent_happyPath() throws JacksonException {
        when(reportInstanceRepository.findById(any())).thenReturn(Optional.of(reportInstance));
        when(mapper.writeValueAsString(any())).thenReturn("{}");
        //noinspection rawtypes
        when((ReportInterface) reportRegistry.get(reportId)).thenReturn(reportInterfaceImplementation);
        when(reportInterfaceImplementation.generateReportData(reportInstance)).thenReturn(reportData);
        when(reportData.getNumberOfRecords()).thenReturn(2L);
        when(reportBlobStore.storeReport(any())).thenReturn(LOCATION);
        when(reportRepository.findById(reportId)).thenReturn(Optional.of(reportEntity));
        when(reportEntity.getReportId()).thenReturn(reportId);
        when(reportEntity.getRetentionPeriod()).thenReturn(Duration.ofDays(1));
        //Act
        genericReportService.generateReportInstanceContent(1L);
        //Assert
        ArgumentCaptor<String> toSaveInBlobStore = ArgumentCaptor.forClass(String.class);
        verify(reportBlobStore).storeReport(toSaveInBlobStore.capture());
        ArgumentCaptor<ReportInstanceEntity> entities = ArgumentCaptor.forClass(ReportInstanceEntity.class);
        verify(reportInstanceRepository, times(2)).save(entities.capture());

        ReportInstanceEntity lastEntity = entities.getAllValues().getLast();
        assertThat(lastEntity.getReport().getReportId()).isEqualTo(reportId);
        assertThat(lastEntity.getGenerationStatus()).isEqualTo(READY);
        assertThat(lastEntity.getLocation()).isEqualTo(LOCATION);
        assertThat(lastEntity.getErrors()).isNull();
        assertThat(lastEntity.getNoOfRecords()).isEqualTo((short) 2);
        assertThat(lastEntity.getCreatedTimestamp()).isEqualTo(LocalDateTime.now(clock));
        assertThat(lastEntity.getScheduledDeletionTimestamp()).isEqualTo(LocalDateTime.now(clock).plusDays(1));
    }

    @Test
    void generateReportInstanceContent_retentionPeriodIsNull_doNotSetDeletionTimestamp()
        throws JacksonException {
        reportInstance.setReportId(reportId);
        when(reportInstanceRepository.findById(any())).thenReturn(Optional.of(reportInstance));
        when(mapper.writeValueAsString(any())).thenReturn("{}");
        //noinspection rawtypes
        when((ReportInterface) reportRegistry.get(reportId)).thenReturn(reportInterfaceImplementation);
        when(reportInterfaceImplementation.generateReportData(reportInstance)).thenReturn(reportData);
        when(reportData.getNumberOfRecords()).thenReturn(2L);
        when(reportBlobStore.storeReport(any())).thenReturn(LOCATION);
        when(reportRepository.findById(reportId)).thenReturn(Optional.of(reportEntity));
        when(reportEntity.getReportId()).thenReturn(reportId);
        when(reportEntity.getRetentionPeriod()).thenReturn(null);
        //Act
        genericReportService.generateReportInstanceContent(1L);
        //Assert
        ArgumentCaptor<ReportInstanceEntity> entities = ArgumentCaptor.forClass(ReportInstanceEntity.class);
        verify(reportInstanceRepository, times(2)).save(entities.capture());
        ReportInstanceEntity lastEntity = entities.getAllValues().getLast();
        assertNull(lastEntity.getScheduledDeletionTimestamp());
    }

    @Test
    void generateReportInstanceContent_reportIdNotFound_throwsExceptionAndDoesNotSaveAnEmptyEntity() {
        //Arrange
        when(reportInstanceRepository.findById(any())).thenThrow(EntityNotFoundException.class);
        //Act
        assertThrows(EntityNotFoundException.class, () -> genericReportService.generateReportInstanceContent(1L));
        //Assert
        verify(reportInstanceRepository, times(0)).save(reportInstance);
    }

    @Test
    void generateReportInstanceContent_unableToSaveToBlobStore_throwsException() throws JacksonException {
        //Arrange
        when(reportInstanceRepository.findById(any())).thenReturn(Optional.of(reportInstance));
        when(mapper.writeValueAsString(any())).thenReturn("{}");
        //noinspection rawtypes
        when((ReportInterface) reportRegistry.get(reportId)).thenReturn(reportInterfaceImplementation);
        when(reportInterfaceImplementation.generateReportData(reportInstance)).thenReturn(reportData);
        when(reportBlobStore.storeReport(any())).thenThrow(RuntimeException.class);
        when(reportEntity.getReportId()).thenReturn(reportId);
        //Act
        assertThrows(ReportGenerationException.class, () -> genericReportService.generateReportInstanceContent(1L));
        //Assert
        ArgumentCaptor<ReportInstanceEntity> entities = ArgumentCaptor.forClass(ReportInstanceEntity.class);
        verify(reportInstanceRepository, times(2)).save(entities.capture());
        ReportInstanceEntity savedEntity = entities.getAllValues().getLast();
        assertThat(savedEntity.getGenerationStatus()).isEqualTo(ERROR);
        assertThat(savedEntity.getErrors()).isInstanceOf(ReportError.class);
    }

    @Test
    void generateReportInstanceContent_noImplementationForReportIdFound_throwsException() {
        //Arrange
        when(reportInstanceRepository.findById(any())).thenReturn(Optional.of(reportInstance));
        //noinspection rawtypes
        when((ReportInterface) reportRegistry.get(reportId)).thenThrow(ReportNotFoundException.class);
        //Act
        assertThrows(ReportGenerationException.class, () -> genericReportService.generateReportInstanceContent(1L));
        //Assert
        ArgumentCaptor<ReportInstanceEntity> entities = ArgumentCaptor.forClass(ReportInstanceEntity.class);
        verify(reportInstanceRepository, times(1)).save(entities.capture());
        ReportInstanceEntity savedEntity = entities.getAllValues().getFirst();
        assertThat(savedEntity.getGenerationStatus()).isEqualTo(ERROR);
        assertThat(savedEntity.getErrors()).isInstanceOf(ReportError.class);
    }

    @Test
    void generateReportInstanceContent_unableToSaveToDb_throwsException() {
        //Arrange
        when(reportInstanceRepository.findById(any())).thenReturn(Optional.of(reportInstance));
        //noinspection rawtypes
        when((ReportInterface) reportRegistry.get(reportId)).thenReturn(reportInterfaceImplementation);
        when(reportInstanceRepository.save(any())).thenThrow(RuntimeException.class);
        when(reportEntity.getReportId()).thenReturn(reportId);
        //Act + Assert
        assertThrows(ReportGenerationException.class, () -> genericReportService.generateReportInstanceContent(1L));
    }

    @Test
    public void addReportInstance_success_singleBU() throws JsonProcessingException {
        //setup
        when(userStateService.checkForAuthorisedUser("")).thenReturn(userState);
        when(userState.getBusinessUnitUser()).thenReturn(Set.of(businessUnitUser1));
        when(reportRepository.findById(reportId)).thenReturn(Optional.of(reportEntity));
        when(reportEntity.getSupportsMultiBu()).thenReturn(false);
        when(reportEntity.getCanManuallyCreate()).thenReturn(true);
        when(mapper.writeValueAsString(any())).thenReturn("{}");
        when(reportInstanceRepository.save(any())).thenReturn(reportInstance);
        when(reportInstanceMapper.toResponseDto(reportInstance)).thenReturn(reportInstanceResponse);
        when(reportParameterValidator.validateReportInstanceParameterValues(reportParameters, reportEntity))
            .thenReturn(true);

        when(businessUnitUser1.getBusinessUnitId()).thenReturn((short)1);
        reportInstance.setReportInstanceId(123L);

        //test
        assertThat(genericReportService.addReportInstance(
            CreateReportInstanceRequestReports.builder()
                .reportId(reportId)
                .reportName(null)
                .businessUnitIds(List.of(1))
                .reportParameters(reportParameters)
                .build(), true)).isEqualTo(reportInstanceResponse);

        verify(reportQueuePublisher).publish(123L);
    }

    @Test
    public void addReportInstance_success_multiBU() throws JsonProcessingException {
        //setup
        when(userStateService.checkForAuthorisedUser("")).thenReturn(userState);
        when(userState.getBusinessUnitUser()).thenReturn(Set.of(businessUnitUser1, businessUnitUser2));
        when(reportRepository.findById(reportId)).thenReturn(Optional.of(reportEntity));
        when(reportEntity.getSupportsMultiBu()).thenReturn(true);
        when(reportEntity.getCanManuallyCreate()).thenReturn(true);
        when(mapper.writeValueAsString(any())).thenReturn("{}");
        when(reportInstanceRepository.save(any())).thenReturn(reportInstance);
        when(reportInstanceMapper.toResponseDto(reportInstance)).thenReturn(reportInstanceResponse);
        when(reportParameterValidator.validateReportInstanceParameterValues(reportParameters, reportEntity))
            .thenReturn(true);

        when(businessUnitUser1.getBusinessUnitId()).thenReturn((short)1);
        when(businessUnitUser2.getBusinessUnitId()).thenReturn((short)2);

        reportInstance.setReportInstanceId(123L);

        //test
        assertThat(genericReportService.addReportInstance(
            CreateReportInstanceRequestReports.builder()
                .reportId(reportId)
                .reportName(null)
                .businessUnitIds(List.of(1, 2))
                .reportParameters(reportParameters)
                .build(), true)).isEqualTo(reportInstanceResponse);

        verify(reportQueuePublisher).publish(123L);
    }

    @Test
    public void addReportInstance_multiBU_notAllowed_throwsException() {
        //setup
        //when(userStateService.checkForAuthorisedUserInSecurityContextHolder()).thenReturn(userState);
        //when(userState.getBusinessUnitUser()).thenReturn(Set.of(businessUnitUser1, businessUnitUser2));
        when(reportRepository.findById(reportId)).thenReturn(Optional.of(reportEntity));
        when(reportEntity.getSupportsMultiBu()).thenReturn(false);

        //test
        UnprocessableException exception = assertThrows(UnprocessableException.class,
            () -> genericReportService.addReportInstance(
                CreateReportInstanceRequestReports.builder()
                    .reportId(reportId)
                    .reportName(null)
                    .businessUnitIds(List.of(1, 2))
                    .reportParameters(reportParameters)
                    .build(), true));
        assertEquals("Too many business units supplied, this report only allows 1", exception.getDetailedReason());
    }

    @Test
    public void addReportInstance_noManualCreation_throwsException() {
        //setup
        when(reportRepository.findById(reportId)).thenReturn(Optional.of(reportEntity));
        when(reportEntity.getSupportsMultiBu()).thenReturn(false);
        when(reportEntity.getCanManuallyCreate()).thenReturn(false);

        //test
        UnprocessableException exception = assertThrows(UnprocessableException.class,
            () -> genericReportService.addReportInstance(
                CreateReportInstanceRequestReports.builder()
                    .reportId(reportId)
                    .reportName(null)
                    .businessUnitIds(List.of(1))
                    .reportParameters(reportParameters)
                    .build(), true));
        assertEquals("This report cannot be manually created", exception.getDetailedReason());
    }

    @Test
    public void addReportInstance_userNotAuthorizedWithBU_throwsException() {
        //setup
        when(userStateService.checkForAuthorisedUser("")).thenReturn(userState);
        when(userState.getBusinessUnitUser()).thenReturn(Set.of(businessUnitUser1, businessUnitUser2));
        when(reportRepository.findById(reportId)).thenReturn(Optional.of(reportEntity));
        when(reportEntity.getSupportsMultiBu()).thenReturn(true);
        when(reportEntity.getCanManuallyCreate()).thenReturn(true);
        //when(mapper.writeValueAsString(any())).thenReturn("{}");

        when(businessUnitUser1.getBusinessUnitId()).thenReturn((short)1);
        //test
        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
            () -> genericReportService.addReportInstance(
                CreateReportInstanceRequestReports.builder()
                    .reportId(reportId)
                    .reportName(null)
                    .businessUnitIds(List.of(1, 2))
                    .reportParameters(reportParameters)
                    .build(), true));
        assertEquals("You cannot generate reports for other business units", exception.getMessage());
    }

    @Test
    public void addReportInstance_failsValidation_throwsException() {
        //setup
        when(userStateService.checkForAuthorisedUser("")).thenReturn(userState);
        when(userState.getBusinessUnitUser()).thenReturn(Set.of(businessUnitUser1, businessUnitUser2));
        when(reportRepository.findById(reportId)).thenReturn(Optional.of(reportEntity));
        when(reportEntity.getSupportsMultiBu()).thenReturn(true);
        when(reportEntity.getCanManuallyCreate()).thenReturn(true);
        //when(mapper.writeValueAsString(any())).thenReturn("{}");
        when(reportParameterValidator.validateReportInstanceParameterValues(reportParameters, reportEntity))
            .thenReturn(false);

        when(businessUnitUser1.getBusinessUnitId()).thenReturn((short)1);
        //test
        UnprocessableException exception = assertThrows(UnprocessableException.class,
            () -> genericReportService.addReportInstance(
                CreateReportInstanceRequestReports.builder()
                    .reportId(reportId)
                    .reportName(null)
                    .businessUnitIds(List.of(1))
                    .reportParameters(reportParameters)
                    .build(), true));
        assertEquals("Validation failed for report instance parameters", exception.getDetailedReason());
    }

    @Test
    public void addReportInstance_genReportAsyncFalse_throwsException() throws JsonProcessingException {
        //setup
        when(userStateService.checkForAuthorisedUser("")).thenReturn(userState);
        when(userState.getBusinessUnitUser()).thenReturn(Set.of(businessUnitUser1));
        when(reportRepository.findById(reportId)).thenReturn(Optional.of(reportEntity));
        when(reportEntity.getSupportsMultiBu()).thenReturn(false);
        when(reportEntity.getCanManuallyCreate()).thenReturn(true);
        when(mapper.writeValueAsString(any())).thenReturn("{}");
        when(reportInstanceRepository.save(any())).thenReturn(reportInstance);
        when(reportParameterValidator.validateReportInstanceParameterValues(reportParameters, reportEntity))
            .thenReturn(true);

        when(businessUnitUser1.getBusinessUnitId()).thenReturn((short)1);

        //todo add to queue setup/asserts?
        //test
        UnprocessableException exception = assertThrows(UnprocessableException.class,
            () -> genericReportService.addReportInstance(
                CreateReportInstanceRequestReports.builder()
                    .reportId(reportId)
                    .reportName(null)
                    .businessUnitIds(List.of(1))
                    .reportParameters(reportParameters)
                    .build(), false));
        assertEquals("generateReportContentAsync cannot be false", exception.getDetailedReason());
    }

    static class TestData implements ReportDataInterface {

        @Override
        public long getNumberOfRecords() {
            return 0;
        }

        @Override
        public ReportMetaData getReportMetaData() {
            return null;
        }
    }

}