package uk.gov.hmcts.opal.service.report;

import static uk.gov.hmcts.opal.common.util.SecurityUtil.getOpalJwtAuthenticationTokenForCurrentUser;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.common.spring.security.OpalJwtAuthenticationToken;
import uk.gov.hmcts.opal.common.user.authorisation.model.BusinessUnitUser;
import uk.gov.hmcts.opal.common.user.authorisation.model.Domain;
import uk.gov.hmcts.opal.common.user.authorisation.model.DomainBusinessUnitUsers;
import uk.gov.hmcts.opal.entity.ReportEntity;
import uk.gov.hmcts.opal.repository.ReportRepository;
import uk.gov.hmcts.opal.service.UserStateService;

@Service
@RequiredArgsConstructor
public class ReportInstanceSearchService {

    private final ReportRepository reportRepository;
    private final UserStateService userStateService;

    public List<ReportEntity> findPermittedReports() {
        OpalJwtAuthenticationToken authToken = getOpalJwtAuthenticationTokenForCurrentUser();
        return reportRepository.findAll().stream()
            .filter(report -> authToken.hasPermission(report.getPermission().toCommonPermission()))
            .toList();
    }

    public ReportEntity findRequestedReportElseThrowError(String reportId) {
        if (reportId != null && !reportId.isBlank()) {
            ReportEntity report = reportRepository.findById(reportId).orElseThrow(EntityNotFoundException::new);
            OpalJwtAuthenticationToken authToken = getOpalJwtAuthenticationTokenForCurrentUser();
            if (!authToken.hasPermission(report.getPermission().toCommonPermission())) {
                throw new AccessDeniedException("User does not have permission for reportId: " + reportId);
            }
            return report;
        }

        return null;
    }

    public List<Long> validateBusinessUnitIds(List<Integer> requestedBusinessUnitIds) {
        List<Long> permittedBusinessUnitIds = getBusinessUnitUsers().stream()
            .map(buUser -> buUser.getBusinessUnitId().longValue())
            .distinct()
            .toList();

        if (requestedBusinessUnitIds == null || requestedBusinessUnitIds.isEmpty()) {
            return permittedBusinessUnitIds;
        }

        List<Long> requestedIds = requestedBusinessUnitIds.stream()
            .filter(Objects::nonNull)
            .map(Integer::longValue)
            .distinct()
            .toList();

        for (Long requestedId : requestedIds) {
            if (!permittedBusinessUnitIds.contains(requestedId)) {
                throw new AccessDeniedException(
                    "User does not have permission for business unit: " + requestedId
                );
            }
        }

        return requestedIds;
    }

    public Map<String, List<Long>> findPermittedReportForBusinessUnits(
        List<ReportEntity> reports,
        List<Long> businessUnitIds) {
        if (reports == null || reports.isEmpty() || businessUnitIds == null || businessUnitIds.isEmpty()) {
            return Map.of();
        }

        List<BusinessUnitUser> businessUnitUsers = getBusinessUnitUsers().stream()
            .filter(buUser -> businessUnitIds.contains(buUser.getBusinessUnitId().longValue()))
            .toList();

        return businessUnitUsers.stream()
            .flatMap(buUser ->
                reports.stream()
                    .filter(report -> buUser.getPermissions().stream()
                        .anyMatch(permission -> permission.getPermissionId() == report.getPermission().getId()))
                    .map(report -> Map.entry(report.getReportId(), buUser.getBusinessUnitId().longValue())))
            .collect(Collectors.groupingBy(
                Map.Entry::getKey,
                Collectors.mapping(Map.Entry::getValue, Collectors.toList())
            ));
    }

    private List<BusinessUnitUser> getBusinessUnitUsers() {
        DomainBusinessUnitUsers domainBusinessUnitUsers = userStateService.getUserStateFromSecurityContext()
            .getDomainBusinessUnitUsers(Domain.FINES);

        if (domainBusinessUnitUsers == null
            || domainBusinessUnitUsers.getBusinessUnitUsers() == null
            || domainBusinessUnitUsers.getBusinessUnitUsers().isEmpty()) {
            return List.of();
        }

        return domainBusinessUnitUsers.getBusinessUnitUsers();
    }
}
