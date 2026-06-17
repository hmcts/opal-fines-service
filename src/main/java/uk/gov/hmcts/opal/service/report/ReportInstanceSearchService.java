package uk.gov.hmcts.opal.service.report;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.entity.ReportEntity;
import uk.gov.hmcts.opal.repository.ReportRepository;
import uk.gov.hmcts.opal.service.UserStateService;

@Service
@RequiredArgsConstructor
public class ReportInstanceSearchService {

    private final ReportRepository reportRepository;
    private final UserStateService userStateService;

    public List<ReportEntity> findPermittedReports() {
        return reportRepository.findAll().stream()
            .filter(report -> userStateService.checkAnyBusinessUnitUserHasPermission(report.getPermission()))
            .toList();
    }

    public ReportEntity findRequestedReportElseThrowError(List<ReportEntity> permittedReports, String reportId) {
        if (reportId != null && !reportId.isBlank()) {
            return permittedReports.stream()
                .filter(report -> reportId.equals(report.getReportId()))
                .findFirst()
                .orElseThrow(() -> {
                    if (reportRepository.existsById(reportId)) {
                        throw new AccessDeniedException("User does not have permission for reportId: " + reportId);
                    }
                    throw new EntityNotFoundException();
                });
        }
        return null;
    }

    public List<Long> findPermittedBusinessUnitIds() {
        return userStateService.getAllBusinessUnitUsersForCurrentUser()
            .stream()
            .map(buUser -> buUser.getBusinessUnitId().longValue())
            .distinct()
            .toList();
    }

    public List<Long> findSelectedBusinessUnitIdsElseThrowError(
        List<Long> permittedBusinessUnitIds,
        List<Integer> requestedBusinessUnitIds
    ) {
        if (requestedBusinessUnitIds == null || requestedBusinessUnitIds.isEmpty()) {
            return permittedBusinessUnitIds;
        }

        List<Long> requestedIds = requestedBusinessUnitIds.stream()
            .filter(Objects::nonNull)
            .map(Integer::longValue)
            .distinct()
            .toList();

        List<Long> selectedBusinessUnitIds = permittedBusinessUnitIds.stream()
            .filter(requestedIds::contains)
            .toList();

        if (selectedBusinessUnitIds.size() != requestedIds.size()) {
            throw new AccessDeniedException("User does not have permission for one or more specified business units");
        }

        return selectedBusinessUnitIds;
    }

    public Map<String, List<Long>> findPermittedReportForBusinessUnits(
        List<ReportEntity> reports,
        List<Long> businessUnitIds) {
        if (reports == null || reports.isEmpty() || businessUnitIds == null || businessUnitIds.isEmpty()) {
            return Map.of();
        }

        return userStateService.getBusinessUnitUsersForBusinessUnitIds(businessUnitIds).stream()
            .flatMap(buUser ->
                reports.stream()
                    .filter(report -> buUser.getPermissions().contains(report.getPermission().toUserPermission()))
                    .map(report -> Map.entry(report.getReportId(), buUser.getBusinessUnitId().longValue())))
            .collect(Collectors.groupingBy(
                Map.Entry::getKey,
                Collectors.mapping(Map.Entry::getValue, Collectors.toList())
            ));
    }
}
