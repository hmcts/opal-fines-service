package uk.gov.hmcts.opal.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.common.user.authorisation.exception.PermissionNotAllowedException;
import uk.gov.hmcts.opal.common.user.authorisation.model.Domain;
import uk.gov.hmcts.opal.entity.CourtFeeEntity;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;
import uk.gov.hmcts.opal.generated.model.CourtFeesResponse;
import uk.gov.hmcts.opal.mapper.CourtFeeMapper;
import uk.gov.hmcts.opal.repository.CourtFeeRepository;
import uk.gov.hmcts.opal.service.opal.BusinessUnitService;

@Service
@Slf4j(topic = "opal.CourtFeesService")
@RequiredArgsConstructor
public class CourtFeesService {
    private final UserStateService userStateService;
    private final BusinessUnitService businessUnitService;
    private final CourtFeeRepository courtFeeRepository;
    private final CourtFeeMapper courtFeeMapper;

    public CourtFeesResponse getCourtFeesForBusinessUnit(Short businessUnitId) {
        BusinessUnitEntity businessUnit = businessUnitService.getBusinessUnit(businessUnitId);
        userStateService.getUserStateFromSecurityContext()
            .getDomainBusinessUnitUsers(Domain.FINES)
            .getBusinessUnitUserForBusinessUnit(businessUnitId)
            .orElseThrow(() -> new PermissionNotAllowedException(businessUnitId));

        List<CourtFeeEntity> courtFees = courtFeeRepository.findAllByBusinessUnit(businessUnit);
        return courtFeeMapper.toCourtFeesResponse(courtFees);
    }
}
