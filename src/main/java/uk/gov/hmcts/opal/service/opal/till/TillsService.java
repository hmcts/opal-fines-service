package uk.gov.hmcts.opal.service.opal.till;

import static uk.gov.hmcts.opal.authorisation.model.FinesPermission.PROCESS_AND_ALLOCATE_PAYMENTS;
import static uk.gov.hmcts.opal.entity.AssociatedRecordType.DEFENDANT_ACCOUNTS;
import static uk.gov.hmcts.opal.entity.DestinationType.F;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.common.user.authorisation.exception.PermissionNotAllowedException;
import uk.gov.hmcts.opal.entity.PaymentInEntity;
import uk.gov.hmcts.opal.entity.TillEntity;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.generated.model.TillsGetResponse;
import uk.gov.hmcts.opal.mapper.till.GetTillMapper;
import uk.gov.hmcts.opal.repository.DefendantAccountRepository;
import uk.gov.hmcts.opal.repository.PaymentInRepository;
import uk.gov.hmcts.opal.repository.TillRepository;
import uk.gov.hmcts.opal.service.UserStateService;

@Service
@RequiredArgsConstructor
public class TillsService {

    private final TillRepository tillRepository;
    private final PaymentInRepository paymentInRepository;
    private final DefendantAccountRepository defendantAccountRepository;
    private final UserStateService userStateService;
    private final GetTillMapper getTillMapper;

    @Transactional(readOnly = true)
    public TillsGetResponse getTill(Long tillId) {
        TillEntity till = findTill(tillId);
        checkPermission(till);
        List<PaymentInEntity> payments = findPayments(tillId);
        Map<Long, DefendantAccountEntity> defendantAccounts = loadDefendantAccounts(payments);

        return getTillMapper.toResponse(till, payments, defendantAccounts);
    }

    private TillEntity findTill(Long tillId) {
        return tillRepository.findById(tillId)
            .orElseThrow(() -> new EntityNotFoundException("Till not found with id: " + tillId));
    }

    private List<PaymentInEntity> findPayments(Long tillId) {
        List<PaymentInEntity> payments =
            paymentInRepository.findByTillEntity_TillIdOrderByPaymentDateAscPaymentInIdAsc(tillId);
        if (payments.isEmpty()) {
            throw new IllegalStateException("Till " + tillId + " has no payments in");
        }
        return payments;
    }

    private void checkPermission(TillEntity till) {
        Short businessUnitId = getBusinessUnitId(till);
        if (!userStateService.getPermittedBusinessUnitIds(List.of(businessUnitId),
            PROCESS_AND_ALLOCATE_PAYMENTS).contains(businessUnitId)) {
            throw new PermissionNotAllowedException(businessUnitId, PROCESS_AND_ALLOCATE_PAYMENTS);
        }
    }

    private Map<Long, DefendantAccountEntity> loadDefendantAccounts(List<PaymentInEntity> payments) {
        List<Long> defendantAccountIds = payments.stream()
            .filter(payment -> payment.getDestinationType() == F)
            .map(TillsService::getDefendantAccountId)
            .distinct()
            .toList();
        if (defendantAccountIds.isEmpty()) {
            return Map.of();
        }

        return defendantAccountRepository.findAllByDefendantAccountIdIn(defendantAccountIds).stream()
            .collect(Collectors.toMap(DefendantAccountEntity::getDefendantAccountId, Function.identity()));
    }

    private static Short getBusinessUnitId(TillEntity till) {
        BusinessUnitEntity businessUnit = till.getBusinessUnit();
        if (businessUnit == null || businessUnit.getBusinessUnitId() == null) {
            throw new IllegalStateException("Till " + till.getTillId() + " has no business unit");
        }
        return businessUnit.getBusinessUnitId();
    }

    private static Long getDefendantAccountId(PaymentInEntity payment) {
        if (payment.getAssociatedRecordType() != DEFENDANT_ACCOUNTS) {
            throw new IllegalStateException("Fine payment " + payment.getPaymentInId()
                + " does not reference a defendant account");
        }

        String associatedRecordId = payment.getAssociatedRecordId();
        if (associatedRecordId == null || associatedRecordId.isBlank()) {
            throw new IllegalStateException("Fine payment " + payment.getPaymentInId()
                + " has no associated_record_id");
        }
        try {
            return Long.valueOf(associatedRecordId);
        } catch (NumberFormatException exception) {
            throw new IllegalStateException("Fine payment " + payment.getPaymentInId()
                + " has an invalid associated_record_id: " + associatedRecordId, exception);
        }
    }
}
