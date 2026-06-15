package uk.gov.hmcts.opal.service.report.util;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.access.AccessDeniedException;
import uk.gov.hmcts.opal.entity.ReportEntity;
import uk.gov.hmcts.opal.repository.ReportRepository;
import uk.gov.hmcts.opal.service.UserStateService;

public class ReportInstanceUtil {

    private ReportInstanceUtil() {
        // utility
    }

    public static ReportEntity throwErrorIfReportIsProvidedButNotPermitted(ReportRepository reportRepository,
        UserStateService userStateService,
        String reportId) {
        if (reportId != null && !reportId.isBlank()) {
            ReportEntity reportEntity = reportRepository.findById(reportId).orElseThrow(EntityNotFoundException::new);
            if (!userStateService.checkAnyBusinessUnitUserHasPermission(reportEntity.getPermission())) {
                throw new AccessDeniedException("User does not have permission for reportId: " + reportId);
            }
            return reportEntity;
        }
        return null;
    }

    public static void throwErrorIfAnyBusinessUnitIsProvidedButNotPermitted(UserStateService userStateService,
        List<Integer> businessUnitIds) {
        if (businessUnitIds != null && !businessUnitIds.isEmpty()) {
            Set<Integer> permittedBusinessUnitIds = userStateService.getAllBusinessUnitUsersForCurrentUser()
                .stream()
                .map(buUser -> buUser.getBusinessUnitId().intValue())
                .collect(Collectors.toSet());

            boolean hasUnpermittedBusinessUnit = businessUnitIds.stream()
                .filter(Objects::nonNull)
                .anyMatch(id -> !permittedBusinessUnitIds.contains(id));

            if (hasUnpermittedBusinessUnit) {
                throw new AccessDeniedException(
                    "User does not have permission for one or more specified business units");
            }
        }
    }


    public static Map<String, List<Long>> findPermittedReportForBusinessUnits(UserStateService userStateService,
        List<ReportEntity> reports, List<Long> businessUnitIds) {

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
