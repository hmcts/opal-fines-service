package uk.gov.hmcts.opal.service.report;

import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.common.user.authorisation.model.BusinessUnitUser;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.generated.model.CreateReportInstanceRequestReports;
import uk.gov.hmcts.opal.generated.model.CreateReportInstanceResponseReports;
import uk.gov.hmcts.opal.service.UserStateService;

@Service
@RequiredArgsConstructor
public class ReportInstanceCreationService {

    private final UserStateService userStateService;
    private final GenericReportService genericReportService;

    public CreateReportInstanceResponseReports createReportInstance(CreateReportInstanceRequestReports request) {
        UserState userState = userStateService.getUserStateV1FromSecurityContext();
        validateCreateReportInstanceRequest(request, userState);
        return genericReportService.addReportInstance(request, userState.getUserId(), userState.getUserName(), true);
    }

    private void validateCreateReportInstanceRequest(CreateReportInstanceRequestReports request,
        UserState userState) {
        Set<Short> permittedBusinessUnits = userState.getBusinessUnitUser().stream()
            .map(BusinessUnitUser::getBusinessUnitId)
            .collect(Collectors.toSet());
        if (!permittedBusinessUnits.containsAll(request.getBusinessUnitIds())) {
            throw new AccessDeniedException("You cannot generate reports for other business units");
        }
    }
}
