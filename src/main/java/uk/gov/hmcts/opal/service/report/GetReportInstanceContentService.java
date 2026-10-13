package uk.gov.hmcts.opal.service.report;

import static uk.gov.hmcts.opal.common.util.SecurityUtil.getOpalJwtAuthenticationTokenForCurrentUser;

import jakarta.persistence.EntityNotFoundException;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.common.spring.security.OpalJwtAuthenticationToken;
import uk.gov.hmcts.opal.common.user.authorisation.exception.PermissionNotAllowedException;
import uk.gov.hmcts.opal.entity.ReportInstanceEntity;
import uk.gov.hmcts.opal.entity.ReportInstanceFileEntity;
import uk.gov.hmcts.opal.entity.report.SupportedFileType;
import uk.gov.hmcts.opal.exception.MissingStoredReportContentException;
import uk.gov.hmcts.opal.exception.UnsupportedContentTypeException;
import uk.gov.hmcts.opal.repository.ReportInstanceFileRepository;
import uk.gov.hmcts.opal.repository.ReportInstanceRepository;
import uk.gov.hmcts.opal.service.blobstore.ReportBlobStore;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "opal.GetReportInstanceContentService")
public class GetReportInstanceContentService {

    private static final TypeReference<Map<String, Object>> REPORT_CONTENT_TYPE = new TypeReference<>() {
    };

    private final ReportInstanceRepository reportInstanceRepository;
    private final ReportInstanceFileRepository reportInstanceFileRepository;
    private final Clock clock;
    private final ReportRegistry reportRegistry;
    private final ReportBlobStore blobStore;
    private final ObjectMapper mapper;

    @Transactional
    public Object getReportInstanceContent(Long id, FileType fileType) {
        log.debug("Getting report instance content for id={}, fileType={}", id, fileType);

        ReportInstanceEntity instance = reportInstanceRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Report instance not found for id: " + id));

        validateReportAccess(instance);
        validateSupportedContentType(instance, fileType);

        String storedReport = loadRequiredStoredReport(id, instance);

        if (fileType == FileType.JSON) {
            return loadReportAsJson(id, storedReport);
        }

        return loadReportAsFile(id, instance, storedReport, fileType);
    }

    private void validateReportAccess(ReportInstanceEntity instance) {
        FinesPermission permission = instance.getReport().getPermission();
        OpalJwtAuthenticationToken authToken = getOpalJwtAuthenticationTokenForCurrentUser();

        if (permission == null || !authToken.hasPermission(permission)) {
            throw new PermissionNotAllowedException(permission);
        }

        List<Short> businessUnits = instance.getBusinessUnit();
        if (businessUnits == null || businessUnits.isEmpty()) {
            throw new PermissionNotAllowedException(permission);
        }

        for (Short businessUnitId : businessUnits) {
            if (!authToken.hasPermissionInBusinessUnit(permission, businessUnitId)) {
                throw new PermissionNotAllowedException(businessUnitId, permission);
            }
        }
    }

    private String loadRequiredStoredReport(Long id, ReportInstanceEntity instance) {
        UUID location = instance.getLocation();
        if (location == null) {
            throw new EntityNotFoundException("Report instance content not found for id: " + id);
        }

        String storedReport;
        try {
            storedReport = new String(blobStore.getReport(location), StandardCharsets.UTF_8);
        } catch (MissingStoredReportContentException missingStoredReportContentException) {
            throw new MissingStoredReportContentException(id, location);
        }
        return storedReport;
    }

    private void validateSupportedContentType(ReportInstanceEntity instance, FileType fileType) {
        List<SupportedFileType> supportedFileTypes = instance.getReport().getSupportedFileTypes();
        SupportedFileType requestedFileType = SupportedFileType.valueOf(fileType.name());

        if (supportedFileTypes == null || !supportedFileTypes.contains(requestedFileType)) {
            List<String> supportedTypes = supportedFileTypes == null ? List.of() : supportedFileTypes.stream()
                .map(Enum::name)
                .collect(Collectors.toList());

            throw new UnsupportedContentTypeException(
                "report '" + instance.getReport().getReportId() + "'",
                fileType.name(),
                supportedTypes
            );
        }
    }

    private Map<String, Object> loadReportAsJson(Long id, String storedReport) {
        try {
            return mapper.readValue(storedReport, REPORT_CONTENT_TYPE);
        } catch (JacksonException e) {
            throw invalidStoredReportContent(id, e);
        }
    }

    private byte[] loadReportAsFile(Long id, ReportInstanceEntity instance, String storedReport,
        FileType fileType) {
        ReportInterface<?> reportTemplate = reportRegistry.get(instance.getReport().getReportId());
        return retrieveOrGenerateReportContent(id, instance, storedReport, reportTemplate, fileType);
    }

    @SuppressWarnings("unchecked")
    final <T extends ReportDataInterface> byte[] retrieveOrGenerateReportContent(
        Long id,
        ReportInstanceEntity instance,
        String storedReport,
        ReportInterface<?> reportTemplate,
        FileType fileType) {

        Optional<byte[]> existingReportContent = retrieveReportContent(instance, fileType);
        if (existingReportContent.isPresent()) {
            return existingReportContent.get();
        }

        ReportInterface<T> typedReportTemplate = (ReportInterface<T>) reportTemplate;
        T reportData = readStoredReportData(id, instance, storedReport, typedReportTemplate);
        byte[] data = typedReportTemplate.convertReportDataToFileType(instance, reportData, fileType);

        UUID location = blobStore.storeReport(new ByteArrayInputStream(data));

        ReportInstanceFileEntity reportInstanceFileEntity =
            ReportInstanceFileEntity.builder()
                .reportInstanceId(instance.getReportInstanceId())
                .fileType(SupportedFileType.valueOf(fileType.name()))
                .locationUuid(location)
                .createdTimestamp(LocalDateTime.now(clock))
                .lastAccessedTimestamp(LocalDateTime.now(clock))
                .build();

        reportInstanceFileRepository.save(reportInstanceFileEntity);

        return data;
    }

    Optional<byte[]> retrieveReportContent(
        ReportInstanceEntity instance,
        FileType fileType) {

        Optional<ReportInstanceFileEntity> reportInstanceFileOpt = reportInstanceFileRepository
            .findByReportInstanceIdAndFileType(instance.getReportInstanceId(),
                SupportedFileType.valueOf(fileType.name()));

        if (reportInstanceFileOpt.isPresent()) {
            ReportInstanceFileEntity reportInstanceFile = reportInstanceFileOpt.get();
            byte[] data = blobStore.getReport(reportInstanceFile.getLocationUuid());
            reportInstanceFile.setLastAccessedTimestamp(LocalDateTime.now(clock));
            reportInstanceFileRepository.save(reportInstanceFile);
            return Optional.of(data);
        }
        return Optional.empty();
    }

    private <T extends ReportDataInterface> T readStoredReportData(
        Long id,
        ReportInstanceEntity instance,
        String storedReport,
        ReportInterface<T> reportTemplate) {

        try {
            StoredReportContent storedReportContent = mapper.readValue(storedReport, StoredReportContent.class);
            return mapper.convertValue(
                storedReportContent.getReportData(),
                reportTemplate.getStoredReportDataClass(instance)
            );
        } catch (IllegalArgumentException | JacksonException e) {
            throw invalidStoredReportContent(id, e);
        }
    }

    private IllegalStateException invalidStoredReportContent(Long id, Exception exception) {
        return new IllegalStateException("Stored report content is not valid JSON for id: " + id, exception);
    }
}
