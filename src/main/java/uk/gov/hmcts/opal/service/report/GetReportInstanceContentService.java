package uk.gov.hmcts.opal.service.report;

import static uk.gov.hmcts.opal.common.util.SecurityUtil.getOpalJwtAuthenticationTokenForCurrentUser;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Map;
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
import uk.gov.hmcts.opal.repository.ReportInstanceRepository;
import uk.gov.hmcts.opal.service.blobstore.ReportBlobStore;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "opal.GetReportInstanceContentService")
public class GetReportInstanceContentService {

    private static final TypeReference<Map<String, Object>> REPORT_CONTENT_TYPE = new TypeReference<>() {
    };

    private final ReportInstanceRepository reportInstanceRepository;
    private final ReportRegistry reportRegistry;
    private final ReportBlobStore blobStore;
    private final ObjectMapper mapper;

    @Transactional(readOnly = true)
    public Object getReportInstanceContent(Long id, FileType fileType) {
        log.debug("Getting report instance content for id={}, fileType={}", id, fileType);

        ReportInstanceEntity instance = reportInstanceRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Report instance not found for id: " + id));

        validateReportAccess(instance);

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

        List<Integer> businessUnits = instance.getBusinessUnit();
        if (businessUnits == null || businessUnits.isEmpty()) {
            throw new PermissionNotAllowedException(permission);
        }

        for (Integer businessUnitId : businessUnits) {
            short businessUnitIdShort = businessUnitId.shortValue();
            if (!authToken.hasPermissionInBusinessUnit(permission, businessUnitIdShort)) {
                throw new PermissionNotAllowedException(businessUnitIdShort, permission);
            }
        }
    }

    private String loadRequiredStoredReport(Long id, ReportInstanceEntity instance) {
        String location = instance.getLocation();
        if (location == null || location.isBlank()) {
            throw new EntityNotFoundException("Report instance content not found for id: " + id);
        }

        String storedReport = blobStore.getReport(location);
        if (storedReport == null) {
            throw new EntityNotFoundException("Report instance content not found for id: " + id);
        }

        return storedReport;
    }

    private Map<String, Object> loadReportAsJson(Long id, String storedReport) {
        try {
            return mapper.readValue(storedReport, REPORT_CONTENT_TYPE);
        } catch (JacksonException e) {
            throw invalidStoredReportContent(id, e);
        }
    }

    private byte[] loadReportAsFile(Long id, ReportInstanceEntity instance, String storedReport, FileType fileType) {
        ReportInterface<?> reportTemplate = reportRegistry.get(instance.getReport().getReportId());
        return convertReportToFile(id, instance, storedReport, reportTemplate, fileType);
    }

    @SuppressWarnings("unchecked")
    private <T extends ReportDataInterface> byte[] convertReportToFile(
        Long id,
        ReportInstanceEntity instance,
        String storedReport,
        ReportInterface<?> reportTemplate,
        FileType fileType) {

        ReportInterface<T> typedReportTemplate = (ReportInterface<T>) reportTemplate;
        T reportData = readStoredReportData(id, instance, storedReport, typedReportTemplate);
        return typedReportTemplate.convertReportDataToFileType(instance, reportData, fileType);
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
