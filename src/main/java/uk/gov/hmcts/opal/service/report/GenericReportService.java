package uk.gov.hmcts.opal.service.report;

import static uk.gov.hmcts.opal.entity.report.ReportInstanceGenerationStatus.IN_PROGRESS;
import static uk.gov.hmcts.opal.entity.report.ReportInstanceGenerationStatus.READY;
import static uk.gov.hmcts.opal.entity.report.ReportInstanceGenerationStatus.REQUESTED;
import static uk.gov.hmcts.opal.service.report.util.ReportInstanceUtil.findPermittedReportForBusinessUnits;
import static uk.gov.hmcts.opal.service.report.util.ReportInstanceUtil.throwErrorIfAnyBusinessUnitIsProvidedButNotPermitted;
import static uk.gov.hmcts.opal.service.report.util.ReportInstanceUtil.throwErrorIfReportIsProvidedButNotPermitted;
import static uk.gov.hmcts.opal.util.NumberUtils.toLongList;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
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
import uk.gov.hmcts.opal.generated.model.ReportInstanceListReportsInner;
import uk.gov.hmcts.opal.mapper.ReportInstanceMapper;
import uk.gov.hmcts.opal.repository.ReportInstanceRepository;
import uk.gov.hmcts.opal.repository.ReportRepository;
import uk.gov.hmcts.opal.service.UserStateService;
import uk.gov.hmcts.opal.repository.jpa.ReportInstanceSpecs;
import uk.gov.hmcts.opal.service.ReportService;
import uk.gov.hmcts.opal.service.UserStateService;
import uk.gov.hmcts.opal.service.blobstore.ReportBlobStore;
import uk.gov.hmcts.opal.service.messaging.ReportQueuePublisherImpl;


@Service
@RequiredArgsConstructor
@Slf4j(topic = "opal.ReportService")
public class GenericReportService implements GenericReportServiceInterface {

    private final UserStateService userStateService;
    private final ReportInstanceRepository reportInstanceRepository;
    private final ReportRepository reportRepository;
    private final ReportInstanceMapper reportInstanceMapper;
    private final ReportRegistry reportRegistry;
    private final ReportBlobStore blobStore;
    private final Clock clock;
    private final ObjectMapper mapper;
    private final ReportService reportService;
    private final ReportInstanceSearchService reportInstanceSearchService;

    private static void processError(ReportInstanceEntity instance, Exception exception) {
        instance.setGenerationStatus(ReportInstanceGenerationStatus.ERROR);
        instance.setErrors(ReportError.builder()
            .operationId(LogUtil.getOrCreateOpalOperationId())
            .error(String.format("%s: %s", exception.getClass().getName(), exception.getMessage())).build());
    }

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

        UserState userState = userStateService.getUserStateV1FromSecurityContext();

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
        } catch (JacksonException e) {
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

    private String getDataAsJson(ReportDataInterface data) throws JacksonException {
        return mapper.writeValueAsString(
            Data.builder()
                .reportData(data)
                .reportMetaData(data.getReportMetaData())
                .build()
        );
    }

    @Override
    public List<ReportInstanceListReportsInner> searchReportInstances(
        final LocalDate fromDate,
        final LocalDate toDate,
        final List<Integer> businessUnits,
        final Integer userId,
        final String reportId) {
        log.debug("Searching for report instances");

        ReportEntity report = reportInstanceSearchService.throwErrorIfReportIsProvidedButNotPermitted(reportId);
        reportInstanceSearchService.throwErrorIfAnyBusinessUnitIsProvidedButNotPermitted(businessUnits);

        List<ReportEntity> selectedReports;
        if (report != null) {
            selectedReports = List.of(report);
        } else {
            selectedReports = reportRepository.findAll();
        }

        List<Long> selectedBusinessUnitIds;
        if (businessUnits != null && !businessUnits.isEmpty()) {
            selectedBusinessUnitIds = toLongList(businessUnits);
        } else {
            selectedBusinessUnitIds = userStateService.getAllBusinessUnitUsersForCurrentUser()
                .stream()
                .map(buUser -> buUser.getBusinessUnitId().longValue())
                .distinct()
                .toList();
        }

        Map<String, List<Long>> permittedReportForBusinessUnits =
            reportInstanceSearchService.findPermittedReportForBusinessUnits(selectedReports, selectedBusinessUnitIds);

        List<ReportInstanceEntity> reportInstances = new ArrayList<>();
        permittedReportForBusinessUnits.forEach((reportIdKey, businessUnitIds) -> {
            Specification<ReportInstanceEntity> spec =
                ReportInstanceSpecs.build(fromDate, toDate, userId, reportIdKey, businessUnitIds);
            reportInstances.addAll(reportInstanceRepository.findAll(spec));

        });

        log.debug("Found {} report instances", reportInstances.size());

        Map<String, ReportEntity> reportIdToReport = selectedReports.stream()
            .collect(Collectors.toMap(ReportEntity::getReportId, reportEntity -> reportEntity));

        return reportInstances.stream()
            .map(instance -> reportInstanceMapper.toDto(instance, reportIdToReport.get(instance.getReportId())))
            .toList();
    }

    @Builder
    @lombok.Data
    static class Data {

        private ReportDataInterface reportData;
        private ReportMetaData reportMetaData;
    }
}
