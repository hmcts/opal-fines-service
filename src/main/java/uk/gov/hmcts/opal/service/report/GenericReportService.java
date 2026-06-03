package uk.gov.hmcts.opal.service.report;

import static uk.gov.hmcts.opal.entity.report.ReportInstanceGenerationStatus.IN_PROGRESS;
import static uk.gov.hmcts.opal.entity.report.ReportInstanceGenerationStatus.READY;
import static uk.gov.hmcts.opal.entity.report.ReportInstanceGenerationStatus.REQUESTED;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.common.logging.LogUtil;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.entity.ReportEntity;
import uk.gov.hmcts.opal.entity.ReportInstanceEntity;
import uk.gov.hmcts.opal.entity.report.ReportInstanceGenerationStatus;
import uk.gov.hmcts.opal.exception.EntityNotSavedException;
import uk.gov.hmcts.opal.exception.ReportGenerationException;
import uk.gov.hmcts.opal.exception.UnprocessableException;
import uk.gov.hmcts.opal.generated.model.CreateReportInstanceRequestReports;
import uk.gov.hmcts.opal.generated.model.CreateReportInstanceResponseReports;
import uk.gov.hmcts.opal.mapper.ReportInstanceMapper;
import uk.gov.hmcts.opal.repository.ReportInstanceRepository;
import uk.gov.hmcts.opal.repository.ReportRepository;
import uk.gov.hmcts.opal.service.UserStateService;
import uk.gov.hmcts.opal.service.blobstore.ReportBlobStore;
import uk.gov.hmcts.opal.service.messaging.ReportQueuePublisherImpl;

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
    private final UserStateService userStateService;
    private final ReportInstanceMapper reportInstanceMapper;
    private final ReportQueuePublisherImpl reportQueuePublisher;
    private final ReportParameterValidator reportParameterValidator;

    @Override
    public void generateReportInstanceContent(Long id) {
        final LocalDateTime currentTimestamp = LocalDateTime.now(clock);
        ReportInstanceEntity instance =
            reportInstanceRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        try {
            String templateId = instance.getReport().getReportId();
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

    @Transactional
    public CreateReportInstanceResponseReports addReportInstance(CreateReportInstanceRequestReports request,
        boolean generateReportContentAsync) {
        ReportEntity reportEntity = reportRepository.findById(request.getReportId())
            .orElseThrow(EntityNotFoundException::new);
        if (!reportEntity.isSupportsMultiBu() && request.getBusinessUnitIds().size() > 1) {
            throw new UnprocessableException("Too many business units supplied, this report only allows 1");
        }
        if (!reportEntity.isCanManuallyCreate()) {
            throw new UnprocessableException("This report cannot be manually created");
        }

        UserState userState = userStateService.checkForAuthorisedUser("");

        if (!userState.getBusinessUnitUser().stream()
            .map(buUser -> buUser.getBusinessUnitId().intValue()).collect(Collectors.toSet())
            .containsAll(request.getBusinessUnitIds())) {
            throw new AccessDeniedException("You cannot generate reports for other business units");
        }

        if (!reportParameterValidator.validateReportInstanceParameterValues(
                request.getReportParameters(), reportEntity)) {
            throw new UnprocessableException("Validation failed for report instance parameters", true);
        }

        try {
            ReportInstanceEntity reportInstanceEntity = ReportInstanceEntity.builder()
                .report(reportEntity)
                .businessUnit(request.getBusinessUnitIds())
                .requestedBy(userState.getUserId())
                .requestedByName(userState.getUserName())
                .reportParameters(mapper.writeValueAsString(request.getReportParameters()))
                .requestedAt(LocalDateTime.now())
                .generationStatus(REQUESTED)
                .reportName(request.getReportName())
                .build();

            reportInstanceEntity = saveReportInstance(reportInstanceEntity);
            if (generateReportContentAsync) {
                reportQueuePublisher.publish(reportInstanceEntity.getReportInstanceId());
            } else {
                //TODO future ticket generateReportContentAsync false
                throw new UnprocessableException("generateReportContentAsync cannot be false");
            }

            return reportInstanceMapper.toResponseDto(reportInstanceEntity);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Report parameters badly formatted", e);
        }
    }

    private ReportInstanceEntity saveReportInstance(ReportInstanceEntity instance) {
        try {
            return reportInstanceRepository.save(instance);
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
