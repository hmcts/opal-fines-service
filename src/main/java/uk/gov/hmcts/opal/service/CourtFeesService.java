package uk.gov.hmcts.opal.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.entity.CourtFeeEntity;
import uk.gov.hmcts.opal.generated.model.CourtFeesResponse;
import uk.gov.hmcts.opal.mapper.CourtFeeMapper;
import uk.gov.hmcts.opal.repository.CourtFeeRepository;

@Service
@Slf4j(topic = "opal.CourtFeesService")
@RequiredArgsConstructor
public class CourtFeesService {
    private final CourtFeeRepository courtFeeRepository;
    private final CourtFeeMapper courtFeeMapper;

    public CourtFeesResponse getCourtFeesForBusinessUnit(Short businessUnitId) {
        //todo use id or get BU 1st?
        //todo test security? prob don't need to use UserService tho...
        List<CourtFeeEntity> courtFees = courtFeeRepository.findAllByBusinessUnit_businessUnitId(businessUnitId);
        return courtFeeMapper.toCourtFeesResponse(courtFees);
    }
}
