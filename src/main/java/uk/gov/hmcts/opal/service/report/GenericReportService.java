package uk.gov.hmcts.opal.service.report;

import static uk.gov.hmcts.opal.entity.report.ReportInstanceGenerationStatus.IN_PROGRESS;
import static uk.gov.hmcts.opal.entity.report.ReportInstanceGenerationStatus.READY;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import java.time.Clock;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.common.logging.LogUtil;
import uk.gov.hmcts.opal.entity.ReportEntity;
import uk.gov.hmcts.opal.entity.ReportInstanceEntity;
import uk.gov.hmcts.opal.entity.report.ReportInstanceGenerationStatus;
import uk.gov.hmcts.opal.exception.EntityNotSavedException;
import uk.gov.hmcts.opal.exception.ReportGenerationException;
import uk.gov.hmcts.opal.repository.ReportInstanceRepository;
import uk.gov.hmcts.opal.repository.ReportRepository;
import uk.gov.hmcts.opal.service.blobstore.ReportBlobStore;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "opal.ReportService")
public class GenericReportService implements GenericReportServiceInterface {

    private final ReportInstanceRepository reportInstanceRepository;
    private final ReportRepository reportRepository;
    private final ReportRegistry reportRegistry;
    private final ReportBlobStore blobStore;
    private final Clock clock;
    private final ObjectMapper mapper;

    @Override
    public void generateReportInstanceContent(Long id) {
        final LocalDateTime currentTimestamp = LocalDateTime.now(clock);
        ReportInstanceEntity instance =
            reportInstanceRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        try {
            String templateId = instance.getReportId();
            final ReportInterface<?> reportTemplate = reportRegistry.get(templateId);
            instance.setGenerationStatus(IN_PROGRESS);
            instance.setErrors(null);
            saveReportInstance(instance);
            ReportDataInterface data = reportTemplate.generateReportData(instance);
            final String location = blobStore.storeReport(getDataAsJson(data));
            instance.setLocation(location);
            instance.setGenerationStatus(READY);
            instance.setCreatedTimestamp(currentTimestamp);
            instance.setNoOfRecords(data.getNumberOfRecords());
            ReportEntity reportEntity = reportRepository.findById(templateId).orElseThrow(EntityNotFoundException::new);
            var retentionPeriod = reportEntity.getRetentionPeriod();
            if (retentionPeriod != null) {
                instance.setScheduledDeletionTimestamp(currentTimestamp.plus(retentionPeriod));
            }
            saveReportInstance(instance);
        } catch (EntityNotSavedException e) {
            throw new ReportGenerationException("Unable to save report instance", e);
        } catch (Exception e) {
            processError(instance, e);
            saveReportInstance(instance);
            throw new ReportGenerationException("Error generating report instance", e);
        }
    }

    private void saveReportInstance(ReportInstanceEntity instance) {
        try {
            reportInstanceRepository.save(instance);
        } catch (Exception e) {
            throw new EntityNotSavedException("Unable to save report instance", e);
        }
    }

    private static void processError(ReportInstanceEntity instance, Exception exception) {
        instance.setGenerationStatus(ReportInstanceGenerationStatus.ERROR);
        instance.setErrors(ReportError.builder()
            .operationId(LogUtil.getOrCreateOpalOperationId())
            .error(String.format("%s: %s", exception.getClass().getName(), exception.getMessage())).build());
    }

    private String getDataAsJson(ReportDataInterface data) throws JacksonException {
        return mapper.writeValueAsString(
            Data.builder()
                .reportData(data)
                .reportMetaData(data.getReportMetaData())
                .build()
        );
    }

    @Builder
    @lombok.Data
    static class Data {
        private ReportDataInterface reportData;
        private ReportMetaData reportMetaData;
    }

}
