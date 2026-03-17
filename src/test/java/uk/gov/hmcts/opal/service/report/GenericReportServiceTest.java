package uk.gov.hmcts.opal.service.report;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.opal.service.report.ReportInstanceGenerationStatus.ERROR;
import static uk.gov.hmcts.opal.service.report.ReportInstanceGenerationStatus.READY;

import jakarta.persistence.EntityNotFoundException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.entity.ReportEntity;
import uk.gov.hmcts.opal.entity.ReportInstanceEntity;
import uk.gov.hmcts.opal.exception.ReportGenerationException;
import uk.gov.hmcts.opal.exception.ReportNotFoundException;
import uk.gov.hmcts.opal.repository.ReportInstanceRepository;
import uk.gov.hmcts.opal.repository.ReportRepository;
import uk.gov.hmcts.opal.service.blobstore.ReportBlobStore;

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

    String reportId;

    Instant now;

    @BeforeEach
    void setUp() {
        reportInstance = new ReportInstanceEntity();
        reportId = String.valueOf(UUID.randomUUID());
        reportInstance.setReportId(reportId);

        now = Instant.parse("2026-01-01T10:00:00Z");
        when(clock.instant()).thenReturn(now);
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);
    }

    @Test
    void generateReportInstanceContent_happyPath() {
        reportInstance.setReportId(reportId);
        when(reportInstanceRepository.findById(any())).thenReturn(Optional.of(reportInstance));
        //noinspection rawtypes
        when((ReportInterface) reportRegistry.get(reportId)).thenReturn(reportInterfaceImplementation);
        when(reportInterfaceImplementation.generateReportData(reportInstance)).thenReturn(reportData);
        when(reportData.getNumberOfRecords()).thenReturn((short) 2);
        when(reportBlobStore.storeReport(any())).thenReturn(LOCATION);
        when(reportRepository.getByReportId(reportId)).thenReturn(reportEntity);
        when(reportEntity.getRetentionPeriod()).thenReturn(Duration.ofDays(1));
        //Act
        genericReportService.generateReportInstanceContent(1L);
        //Assert
        ArgumentCaptor<String> toSaveInBlobStore = ArgumentCaptor.forClass(String.class);
        verify(reportBlobStore).storeReport(toSaveInBlobStore.capture());
        ArgumentCaptor<ReportInstanceEntity> entities = ArgumentCaptor.forClass(ReportInstanceEntity.class);
        verify(reportInstanceRepository, times(2)).saveAndFlush(entities.capture());

        String dataToSave = toSaveInBlobStore.getAllValues().getFirst();
        assertThat(dataToSave)
            .isEqualTo("{\"reportData\":{\"numberOfRecords\":2,\"reportMetaData\":null},\"reportMetaData\":null}");

        ReportInstanceEntity lastEntity = entities.getAllValues().getLast();
        assertThat(lastEntity.getReportId()).isEqualTo(reportId);
        assertThat(lastEntity.getGenerationStatus()).isEqualTo(READY);
        assertThat(lastEntity.getLocation()).isEqualTo(LOCATION);
        assertThat(lastEntity.getErrors()).isNull();
        assertThat(lastEntity.getNoOfRecords()).isEqualTo((short) 2);
        assertThat(lastEntity.getCreatedTimestamp()).isEqualTo(LocalDateTime.now(clock));
        assertThat(lastEntity.getScheduledDeletionTimestamp()).isEqualTo(LocalDateTime.now(clock).plusDays(1));
    }

    @Test
    void generateReportInstanceContent_retentionPeriodIsNull_doNotSetDeletionTimestamp() {
        reportInstance.setReportId(reportId);
        when(reportInstanceRepository.findById(any())).thenReturn(Optional.of(reportInstance));
        //noinspection rawtypes
        when((ReportInterface) reportRegistry.get(reportId)).thenReturn(reportInterfaceImplementation);
        when(reportInterfaceImplementation.generateReportData(reportInstance)).thenReturn(reportData);
        when(reportData.getNumberOfRecords()).thenReturn((short) 2);
        when(reportBlobStore.storeReport(any())).thenReturn(LOCATION);
        when(reportRepository.getByReportId(reportId)).thenReturn(reportEntity);
        when(reportEntity.getRetentionPeriod()).thenReturn(null);
        //Act
        genericReportService.generateReportInstanceContent(1L);
        //Assert
        ArgumentCaptor<ReportInstanceEntity> entities = ArgumentCaptor.forClass(ReportInstanceEntity.class);
        verify(reportInstanceRepository, times(2)).saveAndFlush(entities.capture());
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
        verify(reportInstanceRepository, times(0)).saveAndFlush(reportInstance);
    }

    @Test
    void generateReportInstanceContent_unableToSaveToBlobStore_throwsException() {
        //Arrange
        when(reportInstanceRepository.findById(any())).thenReturn(Optional.of(reportInstance));
        //noinspection rawtypes
        when((ReportInterface) reportRegistry.get(reportId)).thenReturn(reportInterfaceImplementation);
        when(reportInterfaceImplementation.generateReportData(reportInstance)).thenReturn(reportData);
        when(reportBlobStore.storeReport(any())).thenThrow(RuntimeException.class);
        //Act
        assertThrows(ReportGenerationException.class, () -> genericReportService.generateReportInstanceContent(1L));
        //Assert
        ArgumentCaptor<ReportInstanceEntity> entities = ArgumentCaptor.forClass(ReportInstanceEntity.class);
        verify(reportInstanceRepository, times(2)).saveAndFlush(entities.capture());
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
        verify(reportInstanceRepository, times(1)).saveAndFlush(entities.capture());
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
        when(reportInstanceRepository.saveAndFlush(any())).thenThrow(RuntimeException.class);
        //Act + Assert
        assertThrows(ReportGenerationException.class, () -> genericReportService.generateReportInstanceContent(1L));
    }

    static class TestData implements ReportDataInterface {

        @Override
        public short getNumberOfRecords() {
            return 0;
        }

        @Override
        public ReportMetaData getReportMetaData() {
            return null;
        }
    }

}