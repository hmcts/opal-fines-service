package uk.gov.hmcts.opal.service.report;

import static uk.gov.hmcts.opal.entity.report.ReportInstanceGenerationStatus.IN_PROGRESS;
import static uk.gov.hmcts.opal.entity.report.ReportInstanceGenerationStatus.READY;
import static uk.gov.hmcts.opal.entity.report.ReportInstanceGenerationStatus.REQUESTED;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import uk.gov.hmcts.opal.common.logging.LogUtil;
import uk.gov.hmcts.opal.common.user.authorisation.model.BusinessUnitUser;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.entity.ReportEntity;
import uk.gov.hmcts.opal.entity.ReportInstanceEntity;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;
import uk.gov.hmcts.opal.entity.report.ReportInstanceGenerationStatus;
import uk.gov.hmcts.opal.exception.EntityNotSavedException;
import uk.gov.hmcts.opal.exception.ReportGenerationException;
import uk.gov.hmcts.opal.exception.UnprocessableException;
import uk.gov.hmcts.opal.generated.model.CreateReportInstanceRequestReports;
import uk.gov.hmcts.opal.generated.model.CreateReportInstanceResponseReports;
import uk.gov.hmcts.opal.generated.model.ReportInstanceListReportsInner;
import uk.gov.hmcts.opal.generated.model.ReportInstanceReports;
import uk.gov.hmcts.opal.mapper.ReportInstanceMapper;
import uk.gov.hmcts.opal.repository.BusinessUnitRepository;
import uk.gov.hmcts.opal.repository.ReportInstanceRepository;
import uk.gov.hmcts.opal.repository.ReportRepository;
import uk.gov.hmcts.opal.repository.jpa.ReportInstanceSpecs;
import uk.gov.hmcts.opal.service.UserStateService;
import uk.gov.hmcts.opal.service.blobstore.ReportBlobStore;
import uk.gov.hmcts.opal.service.messaging.ReportQueuePublisher;


@Service
@RequiredArgsConstructor
@Slf4j(topic = "opal.ReportService")
public class GenericReportService implements GenericReportServiceInterface {

    private final ReportParameterValidator reportParameterValidator;
    private final ReportQueuePublisher reportQueuePublisher;
    private final UserStateService userStateService;
    private final ReportInstanceRepository reportInstanceRepository;
    private final ReportRepository reportRepository;
    private final ReportInstanceMapper reportInstanceMapper;
    private final ReportRegistry reportRegistry;
    private final ReportBlobStore blobStore;
    private final Clock clock;
    private final ObjectMapper mapper;
    private final BusinessUnitRepository businessUnitRepository;
    private final ReportInstanceSearchService reportInstanceSearchService;
    private final GetReportInstanceContentService getReportInstanceContentService;

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

    public ReportInstanceReports getReportInstance(Long reportInstanceId) {
        ReportInstanceEntity reportInstanceEntity = reportInstanceRepository.findById(reportInstanceId)
            .orElseThrow(() -> new EntityNotFoundException("Report instance not found with id: " + reportInstanceId));

        UserState userState = userStateService.getUserStateV1FromSecurityContext();

        List<Short> businessUnitIdsForReportInstance = reportInstanceEntity.getBusinessUnit();
        if (!userState.getBusinessUnitUser().stream().map(BusinessUnitUser::getBusinessUnitId)
            .collect(Collectors.toSet()).containsAll(businessUnitIdsForReportInstance)) {
            throw new AccessDeniedException("You cannot request report instances associated with other business units");
        }
        List<BusinessUnitEntity> businessUnitEntities = businessUnitRepository
            .findAllById(businessUnitIdsForReportInstance);

        return reportInstanceMapper.toReportInstanceReportsDto(reportInstanceEntity, businessUnitEntities);
    }

    @Override
    public Object getReportInstanceContent(Long id, FileType fileType) {
        return getReportInstanceContentService.getReportInstanceContent(id, fileType);
    }

    @Transactional
    public CreateReportInstanceResponseReports addReportInstance(CreateReportInstanceRequestReports request,
        Long requestedBy, String requestedByName, boolean generateReportContentAsync) {
        ReportEntity reportEntity = reportRepository.findById(request.getReportId())
            .orElseThrow(EntityNotFoundException::new);
        if (!reportEntity.isSupportsMultiBu() && request.getBusinessUnitIds().size() > 1) {
            throw new UnprocessableException("Too many business units supplied, this report only allows 1");
        }
        if (generateReportContentAsync && !reportEntity.isCanManuallyCreate()) {
            throw new UnprocessableException("This report cannot be manually created");
        }

        validateReportInstanceParameters(request, reportEntity);

        CreateReportInstanceResponseReports response = createReportInstance(
            request,
            reportEntity,
            requestedBy,
            requestedByName
        );
        if (generateReportContentAsync) {
            reportQueuePublisher.publish(response.getReportInstanceId());
        } else {
            generateReportInstanceContent(response.getReportInstanceId());
        }
        return response;
    }

    @Override
    public List<ReportInstanceListReportsInner> searchReportInstances(
        final LocalDate fromDate,
        final LocalDate toDate,
        final List<Short> businessUnits,
        final Long userId,
        final String reportId) {
        log.debug("Searching for report instances");

        List<ReportEntity> selectedReports;
        if (reportId != null && !reportId.isBlank()) {
            selectedReports = List.of(reportInstanceSearchService.findRequestedReportElseThrowError(reportId));
        } else {
            selectedReports = reportInstanceSearchService.findPermittedReports();
        }

        List<Short> selectedBusinessUnitIds = reportInstanceSearchService.validateBusinessUnitIds(businessUnits);

        Map<String, List<Short>> permittedReportForBusinessUnits =
            reportInstanceSearchService.findPermittedReportForBusinessUnits(selectedReports, selectedBusinessUnitIds);

        List<ReportInstanceEntity> reportInstances = new ArrayList<>();
        permittedReportForBusinessUnits.forEach((reportIdKey, businessUnitIds) -> {
            Specification<ReportInstanceEntity> spec =
                ReportInstanceSpecs.build(fromDate, toDate, userId, reportIdKey, businessUnitIds);
            reportInstances.addAll(reportInstanceRepository.findAll(spec));

        });

        log.debug("Found {} report instances", reportInstances.size());

        return reportInstances.stream()
            .map(reportInstanceMapper::toReportInstanceListReportsInnerDto)
            .toList();
    }

    private ReportInstanceEntity saveReportInstance(ReportInstanceEntity instance) {
        try {
            return reportInstanceRepository.save(instance);
        } catch (Exception e) {
            throw new EntityNotSavedException("Unable to save report instance", e);
        }
    }

    private void validateReportInstanceParameters(CreateReportInstanceRequestReports request,
        ReportEntity reportEntity) {
        if (!reportParameterValidator.validateReportInstanceParameterValues(
            request.getReportParameters(), reportEntity)) {
            throw new UnprocessableException("Validation failed for report instance parameters", true);
        }
    }

    private CreateReportInstanceResponseReports createReportInstance(CreateReportInstanceRequestReports request,
        ReportEntity reportEntity, Long requestedBy, String requestedByName) {
        try {
            ReportInstanceEntity reportInstanceEntity = ReportInstanceEntity.builder()
                .report(reportEntity)
                .businessUnit(request.getBusinessUnitIds())
                .requestedBy(requestedBy)
                .requestedByName(requestedByName)
                .reportParameters(mapper.writeValueAsString(request.getReportParameters()))
                .requestedAt(LocalDateTime.now())
                .generationStatus(REQUESTED)
                .reportName(request.getReportName())
                .build();

            reportInstanceEntity = saveReportInstance(reportInstanceEntity);
            return reportInstanceMapper.toResponseDto(reportInstanceEntity);
        } catch (JacksonException e) {
            throw new IllegalArgumentException("Report parameters badly formatted", e);
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

    @Builder
    @lombok.Data
    static class Data {

        private ReportDataInterface reportData;
        private ReportMetaData reportMetaData;
    }
}
