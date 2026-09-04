package uk.gov.hmcts.opal.service.opal;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.entity.businessunit.OutstandingAutoPaymentEntity;
import uk.gov.hmcts.opal.generated.model.BusinessUnitsOutstandingAutoPaymentItem;
import uk.gov.hmcts.opal.generated.model.BusinessUnitsOutstandingAutoPaymentResponse;
import uk.gov.hmcts.opal.mapper.OutstandingAutoPaymentMapper;
import uk.gov.hmcts.opal.repository.OutstandingAutoPaymentRepository;
import uk.gov.hmcts.opal.service.UserStateService;

@Service
@RequiredArgsConstructor
public class OutstandingAutoPaymentService {

    private final OutstandingAutoPaymentRepository repository;

    private final OutstandingAutoPaymentMapper mapper;

    private final UserStateService userStateService;

    @Transactional(readOnly = true)
    public BusinessUnitsOutstandingAutoPaymentResponse getOutstandingAutoPaymentCount() {
        List<Short> permittedBusinessUnitIds =
            userStateService.getBusinessUnitIdsFor(FinesPermission.PROCESS_AND_ALLOCATE_PAYMENTS);

        if (permittedBusinessUnitIds.isEmpty()) {
            return response(List.of());
        }

        List<OutstandingAutoPaymentEntity> counts = repository
            .findByBusinessUnitIdInOrderByBusinessUnitNameAsc(permittedBusinessUnitIds);

        return response(mapper.toItems(counts));
    }

    private BusinessUnitsOutstandingAutoPaymentResponse response(
        List<BusinessUnitsOutstandingAutoPaymentItem> businessUnits) {

        return BusinessUnitsOutstandingAutoPaymentResponse.builder()
            .businessUnits(businessUnits)
            .build();
    }
}
