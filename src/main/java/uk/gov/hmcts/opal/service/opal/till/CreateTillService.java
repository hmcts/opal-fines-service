package uk.gov.hmcts.opal.service.opal.till;

import static uk.gov.hmcts.opal.common.util.SecurityUtil.getOpalJwtAuthenticationTokenForCurrentUser;

import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.common.user.authorisation.exception.PermissionNotAllowedException;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserStateV2;
import uk.gov.hmcts.opal.entity.AssociatedRecordType;
import uk.gov.hmcts.opal.entity.MiscellaneousAccountEntity;
import uk.gov.hmcts.opal.entity.PaymentInEntity;
import uk.gov.hmcts.opal.entity.TillEntity;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.generated.model.TillsCreateAdditionalInformation;
import uk.gov.hmcts.opal.generated.model.TillsCreatePaymentDetails;
import uk.gov.hmcts.opal.generated.model.TillsCreatePaymentIn;
import uk.gov.hmcts.opal.generated.model.TillsCreateRequest;
import uk.gov.hmcts.opal.mapper.till.CreateTillMapper;
import uk.gov.hmcts.opal.repository.BusinessUnitRepository;
import uk.gov.hmcts.opal.repository.DefendantAccountRepository;
import uk.gov.hmcts.opal.repository.PaymentInRepository;
import uk.gov.hmcts.opal.repository.TillRepository;
import uk.gov.hmcts.opal.service.UserStateService;
import uk.gov.hmcts.opal.service.report.PreAllocatedCashTillService;

@Service
@RequiredArgsConstructor
public class CreateTillService {

    private final TillRepository tillRepository;
    private final PaymentInRepository paymentInRepository;
    private final BusinessUnitRepository businessUnitRepository;
    private final PreAllocatedCashTillService preAllocatedCashTillService;
    private final UserStateService userStateService;
    private final CreateTillMapper createTillMapper;
    private final ObjectMapper objectMapper;
    private final Clock clock;
    private final DefendantAccountRepository defendantAccountRepository;
    private final CreateTillPayerService createTillPayerService;

    @Transactional
    public void createTill(TillsCreateRequest request) {
        Short businessUnitId = request.getBusinessUnitId();
        checkPermission(businessUnitId);
        List<TillsCreatePaymentIn> requestPayments = request.getPaymentsIn();
        Short paymentsCount = validatePaymentCount(requestPayments);
        CreateTillValidator.validate(request);
        validateDefendantAccounts(requestPayments, businessUnitId);

        TillCreationContext context = resolveCreationContext(businessUnitId);
        TillEntity till = saveTill(requestPayments, paymentsCount, context);

        savePayments(requestPayments, till, context.createdDate());
        createPreAllocatedReport(till, context.userState());
    }

    private void checkPermission(Short businessUnitId) {
        if (!getOpalJwtAuthenticationTokenForCurrentUser()
            .hasPermissionInBusinessUnit(FinesPermission.PROCESS_AND_ALLOCATE_PAYMENTS, businessUnitId)) {
            throw new PermissionNotAllowedException(businessUnitId, FinesPermission.PROCESS_AND_ALLOCATE_PAYMENTS);
        }
    }

    private TillCreationContext resolveCreationContext(Short businessUnitId) {
        BusinessUnitEntity businessUnit = businessUnitRepository.findById(businessUnitId)
            .orElseThrow(() -> new EntityNotFoundException("Business unit not found: " + businessUnitId));

        UserStateV2 userState = userStateService.getUserStateFromSecurityContext();
        return new TillCreationContext(businessUnit, userState, LocalDateTime.now(clock));
    }

    private TillEntity saveTill(List<TillsCreatePaymentIn> requestPayments, Short paymentsCount,
                                TillCreationContext context) {
        return tillRepository.save(createTillMapper.toTillEntity(
            context.businessUnit(),
            context.userState().getUserId().toString(),
            context.userState().getUsername(),
            nextTillNumber(context.businessUnit().getBusinessUnitId()),
            calculateTotalPaymentAmount(requestPayments),
            paymentsCount,
            context.createdDate()));
    }

    private void savePayments(List<TillsCreatePaymentIn> requestPayments, TillEntity till,
                              LocalDateTime paymentDate) {
        paymentInRepository.saveAll(requestPayments.stream()
            .map(payment -> toPaymentIn(payment, till, paymentDate))
            .toList());
    }

    private void createPreAllocatedReport(TillEntity till, UserStateV2 userState) {
        preAllocatedCashTillService.createPreAllocatedReportInstance(till.getTillId(), userState.getUserId(),
            userState.getUsername());
    }

    private Short nextTillNumber(Short businessUnitId) {
        Long tillNumber = tillRepository.getNextTillNumber(businessUnitId);

        if (tillNumber < Short.MIN_VALUE || tillNumber > Short.MAX_VALUE) {
            throw new IllegalStateException("Till number exceeds supported range for business unit " + businessUnitId);
        }
        return tillNumber.shortValue();
    }

    private static BigDecimal calculateTotalPaymentAmount(List<TillsCreatePaymentIn> payments) {
        return payments.stream()
            .map(payment -> payment.getPaymentDetails().getAmount())
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private PaymentInEntity toPaymentIn(TillsCreatePaymentIn requestPayment, TillEntity till,
                                        LocalDateTime paymentDate) {
        AssociatedRecordType type = null;
        String id = null;
        if (requestPayment.getPaymentDetails().getDestinationType()
            == TillsCreatePaymentDetails.DestinationTypeEnum.F) {
            type = AssociatedRecordType.DEFENDANT_ACCOUNTS;
            id = requestPayment.getDefendantAccountId().toString();
        } else {
            MiscellaneousAccountEntity account = createTillPayerService.createPayerAccount(
                requestPayment.getAdditionalInformation(), till.getBusinessUnit());
            if (account != null) {
                type = AssociatedRecordType.MISCELLANEOUS_ACCOUNTS;
                id = account.getMiscellaneousAccountId().toString();
            }
        }
        return createTillMapper.toPaymentIn(requestPayment, till, paymentDate,
            serializeAdditionalInformation(requestPayment.getAdditionalInformation()), type, id);
    }

    private void validateDefendantAccounts(List<TillsCreatePaymentIn> payments, Short businessUnitId) {
        Set<Long> ids = payments.stream().map(TillsCreatePaymentIn::getDefendantAccountId)
            .filter(Objects::nonNull).collect(Collectors.toSet());
        if (ids.isEmpty()) {
            return;
        }
        Map<Long, DefendantAccountEntity> accounts = defendantAccountRepository.findAllById(ids).stream()
            .collect(Collectors.toMap(DefendantAccountEntity::getDefendantAccountId, Function.identity()));
        for (Long id : ids) {
            DefendantAccountEntity account = accounts.get(id);
            if (account == null || !businessUnitId.equals(account.getBusinessUnit().getBusinessUnitId())) {
                throw badRequest("defendant_account_id " + id
                    + " must identify an existing account in business_unit_id "
                    + businessUnitId);
            }
        }
    }

    private static Short validatePaymentCount(List<TillsCreatePaymentIn> requestPayments) {
        int paymentsCount = requestPayments.size();

        if (paymentsCount > Short.MAX_VALUE) {
            throw badRequest("A till cannot contain more than " + Short.MAX_VALUE + " payments");
        }
        return (short) paymentsCount;
    }

    private static ResponseStatusException badRequest(String reason) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, reason);
    }

    private String serializeAdditionalInformation(TillsCreateAdditionalInformation information) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("payment_received_from", information.getPaymentReceivedFrom().getValue());
        putIfSupplied(metadata, "payment_reference", information.getPaymentReference());
        putIfSupplied(metadata, "transfer_source",
            information.getTransferSource() == null ? null : information.getTransferSource().getValue());
        putIfSupplied(metadata, "court_fee_code", information.getCourtFeeCode());
        putIfSupplied(metadata, "number_of_items", information.getNumberOfItems());
        putIfSupplied(metadata, "suspense_reason_code",
            information.getSuspenseReasonCode() == null ? null : information.getSuspenseReasonCode().getValue());
        putIfSupplied(metadata, "suspense_reason_detail", information.getSuspenseReasonDetail());
        putIfSupplied(metadata, "suspense_note", information.getSuspenseNote());
        try {
            return objectMapper.writeValueAsString(metadata);
        } catch (JacksonException e) {
            throw new IllegalArgumentException("Unable to store payment additional information", e);
        }
    }

    private static void putIfSupplied(Map<String, Object> metadata, String field, Object value) {
        if (value != null) {
            metadata.put(field, value);
        }
    }

    private record TillCreationContext(BusinessUnitEntity businessUnit, UserStateV2 userState,
                                       LocalDateTime createdDate) {
    }
}
