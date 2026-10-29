package uk.gov.hmcts.opal.service.report;

import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.common.user.authorisation.model.BusinessUnitUserV2;
import uk.gov.hmcts.opal.common.user.authorisation.model.Domain;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserStateV2;
import uk.gov.hmcts.opal.generated.model.CreateReportInstanceRequestReports;
import uk.gov.hmcts.opal.generated.model.CreateReportInstanceResponseReports;
import uk.gov.hmcts.opal.service.UserStateService;

@Service
@RequiredArgsConstructor
public class ReportInstanceCreationService {

    private final UserStateService userStateService;
    private final GenericReportService genericReportService;

    public CreateReportInstanceResponseReports createReportInstance(CreateReportInstanceRequestReports request) {
        UserStateV2 userState = userStateService.getUserStateFromSecurityContext();
        validateCreateReportInstanceRequest(request, userState);
        return genericReportService.addReportInstance(request, userState.getUserId(), userState.getUsername(), true);
    }

    private void validateCreateReportInstanceRequest(CreateReportInstanceRequestReports request,
        UserStateV2 userState) {
        Set<Short> permittedBusinessUnits = userState.getDomainBusinessUnitUsers(Domain.FINES)
            .getBusinessUnitUsers().stream()
            .map(BusinessUnitUserV2::getBusinessUnitId)
            .collect(Collectors.toSet());
        if (!permittedBusinessUnits.containsAll(request.getBusinessUnitIds())) {
            throw new AccessDeniedException("You cannot generate reports for other business units");
        }
    }
}
