package uk.gov.hmcts.opal.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.entity.ReportEntity;
import uk.gov.hmcts.opal.entity.ReportInstanceEntity;
import uk.gov.hmcts.opal.entity.report.ReportInstanceGenerationStatus;
import uk.gov.hmcts.opal.exception.ReportGenerationException;
import uk.gov.hmcts.opal.repository.ReportInstanceRepository;
import uk.gov.hmcts.opal.repository.ReportRepository;
import uk.gov.hmcts.opal.service.blobstore.ReportBlobStore;
import uk.gov.hmcts.opal.service.report.FileType;
import uk.gov.hmcts.opal.service.report.GenericReportService;
import uk.gov.hmcts.opal.service.report.ReportDataInterface;
import uk.gov.hmcts.opal.service.report.ReportError;
import uk.gov.hmcts.opal.service.report.ReportInterface;
import uk.gov.hmcts.opal.service.report.ReportMetaData;
import uk.gov.hmcts.opal.service.report.ReportRegistry;
import uk.gov.hmcts.opal.service.report.ReportId;

@Transactional
@DirtiesContext
class GenericReportServiceTest extends AbstractIntegrationTest {

    @Autowired
    private GenericReportService service;

    @Autowired
    private ReportInstanceRepository reportInstanceRepository;

    @Autowired
    private ReportRepository reportRepository;

    @MockitoBean
    private ReportBlobStore blobStore;

    @MockitoBean
    private Clock clock;

    @TestConfiguration
    static class TestBeans {

        @Bean
        @Primary
        ReportRegistry reportRegistry(ReportInterface<TestReportData> reportTemplate) {
            return new ReportRegistry(List.of(reportTemplate));
        }

        @Bean
        @Primary
        ReportInterface<TestReportData> reportTemplate() {
            return new TestReportTemplate();
        }
    }

    @BeforeEach
    void setUp() {
        Instant fixedInstant = Instant.parse("2026-04-07T10:15:30Z");
        when(clock.instant()).thenReturn(fixedInstant);
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);
    }

    @Test
    void generateReportInstanceContent_persistsReadyInstance_happyPath() {
        //Arrange
        String location = String.valueOf(UUID.randomUUID());
        ReportEntity report = reportRepository.save(buildReportEntity("fp_register"));
        ReportInstanceEntity instance = reportInstanceRepository.save(buildReportInstanceEntity(report.getReportId()));
        when(blobStore.storeReport(any(String.class))).thenReturn(location);

        //Act
        service.generateReportInstanceContent(instance.getReportInstanceId());

        //Assert
        ReportInstanceEntity saved = reportInstanceRepository.findById(instance.getReportInstanceId()).orElseThrow();
        assertThat(saved.getGenerationStatus()).isEqualTo(ReportInstanceGenerationStatus.READY);
        assertThat(saved.getLocation()).isEqualTo(location);
        assertThat(saved.getCreatedTimestamp())
            .isEqualTo(LocalDateTime.of(2026, 4, 7, 10, 15, 30));
        assertThat(saved.getScheduledDeletionTimestamp())
            .isEqualTo(LocalDateTime.of(2026, 4, 8, 10, 15, 30));
        assertThat(saved.getNoOfRecords()).isEqualTo((short) 42);
        assertThat(saved.getErrors()).isNull();
    }

    @Test
    void generateReportInstanceContent_persistsReadyInstance_errorCase() {
        //Arrange
        ReportEntity report = reportRepository.save(buildReportEntity("fp_register"));
        ReportInstanceEntity instance = reportInstanceRepository.save(buildReportInstanceEntity(report.getReportId()));
        when(blobStore.storeReport(any(String.class))).thenThrow(new RuntimeException());

        //Act
        assertThrows(ReportGenerationException.class,
            () -> service.generateReportInstanceContent(instance.getReportInstanceId()));

        //Assert
        ReportInstanceEntity saved = reportInstanceRepository.findById(instance.getReportInstanceId()).orElseThrow();
        assertThat(saved.getGenerationStatus()).isEqualTo(ReportInstanceGenerationStatus.ERROR);
        assertThat(saved.getErrors()).isNotNull();
    }

    private ReportEntity buildReportEntity(String id) {
        ReportEntity report = new ReportEntity();
        report.setReportId(id);
        report.setRetentionPeriod(Duration.ofDays(1));
        return report;
    }

    private ReportInstanceEntity buildReportInstanceEntity(String reportId) {
        ReportInstanceEntity instance = new ReportInstanceEntity();
        instance.setReportId(reportId);
        instance.setErrors(new ReportError("Error", "Existing error"));
        instance.setGenerationStatus(ReportInstanceGenerationStatus.ERROR);
        return instance;
    }


    private static class TestReportTemplate implements ReportInterface<TestReportData> {

        @Override
        public ReportId getReportId() {
            return ReportId.FP_REGISTER;
        }

        @Override
        public TestReportData generateReportData(ReportInstanceEntity instance) {
            return new TestReportData(42);
        }

        @Override
        public byte[] convertReportDataToFileType(ReportInstanceEntity reportInstance, TestReportData reportData,
            FileType fileType) {
            return new byte[0];
        }
    }

    private static final class TestReportData implements ReportDataInterface {

        private final int numberOfRecords;

        private TestReportData(int numberOfRecords) {
            this.numberOfRecords = numberOfRecords;
        }

        @Override
        public int getNumberOfRecords() {
            return numberOfRecords;
        }

        @Override
        public ReportMetaData getReportMetaData() {
            return new ReportMetaData(List.of());
        }
    }
}
