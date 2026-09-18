package uk.gov.hmcts.opal.service.opal.till;

import static uk.gov.hmcts.opal.common.util.SecurityUtil.getOpalJwtAuthenticationTokenForCurrentUser;

import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.common.user.authorisation.exception.PermissionNotAllowedException;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserStateV2;
import uk.gov.hmcts.opal.entity.AssociatedRecordType;
import uk.gov.hmcts.opal.entity.PaymentInEntity;
import uk.gov.hmcts.opal.entity.PaymentReceivedFrom;
import uk.gov.hmcts.opal.entity.TillEntity;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;
import uk.gov.hmcts.opal.generated.model.TillsCreateAdditionalInformation;
import uk.gov.hmcts.opal.generated.model.TillsCreatePaymentDetails;
import uk.gov.hmcts.opal.generated.model.TillsCreatePaymentIn;
import uk.gov.hmcts.opal.generated.model.TillsCreateRequest;
import uk.gov.hmcts.opal.mapper.till.CreateTillMapper;
import uk.gov.hmcts.opal.repository.BusinessUnitRepository;
import uk.gov.hmcts.opal.repository.PaymentInRepository;
import uk.gov.hmcts.opal.repository.TillRepository;
import uk.gov.hmcts.opal.service.UserStateService;
import uk.gov.hmcts.opal.service.report.PreAllocatedCashTillService;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

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

    private static final String PAYMENT_RECEIVED_FROM = "payment_received_from";
    private static final String ADDITIONAL_INFORMATION = "additional_information";
    private static final String TRANSFER_SOURCE = "transfer_source";

    @Transactional
    public void createTill(TillsCreateRequest request) {
        Short businessUnitId = request.getBusinessUnitId();
        checkPermission(businessUnitId);
        List<TillsCreatePaymentIn> requestPayments = request.getPaymentsIn();
        Short paymentsCount = validatePaymentCount(requestPayments);
        validatePaymentScope(requestPayments);

        TillCreationContext context = resolveCreationContext(businessUnitId);
        TillEntity till = saveTill(requestPayments, paymentsCount, context);

        savePayments(requestPayments, till, context.createdDate(), request.getAdditionalInformation());
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
                              LocalDateTime paymentDate, TillsCreateAdditionalInformation additionalInformation) {
        paymentInRepository.saveAll(requestPayments.stream()
            .map(payment -> toPaymentIn(payment, till, paymentDate, additionalInformation))
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
        LocalDateTime dateTimeNow, TillsCreateAdditionalInformation additionalInformation) {
        TillsCreatePaymentDetails details = requestPayment.getPaymentDetails();

        return createTillMapper.toPaymentIn(
            requestPayment,
            till,
            dateTimeNow,
            serializeAdditionalInformation(
                createTillMapper.toPaymentReceivedFrom(details.getAdditionalInformation()),
                additionalInformation),
            AssociatedRecordType.DEFENDANT_ACCOUNTS,
            requireDefendantAccountId(requestPayment).toString());
    }

    private static void validatePaymentScope(List<TillsCreatePaymentIn> requestPayments) {
        requestPayments.forEach(CreateTillService::validateFinesPayment);
    }

    private static Short validatePaymentCount(List<TillsCreatePaymentIn> requestPayments) {
        int paymentsCount = requestPayments.size();

        if (paymentsCount > Short.MAX_VALUE) {
            throw badRequest("A till cannot contain more than " + Short.MAX_VALUE + " payments");
        }
        return (short) paymentsCount;
    }

    private static void validateFinesPayment(TillsCreatePaymentIn requestPayment) {
        if (requestPayment.getPaymentDetails().getDestinationType()
            != TillsCreatePaymentDetails.DestinationTypeEnum.FINES) {
            throw badRequest("Only fines payments are supported when creating a till");
        }
        requireDefendantAccountId(requestPayment);
    }

    private static Long requireDefendantAccountId(TillsCreatePaymentIn requestPayment) {
        Long defendantAccountId = requestPayment.getDefendantAccountId();

        if (defendantAccountId == null) {
            throw badRequest("defendant_account_id is required for fines payments");
        }
        return defendantAccountId;
    }

    private static ResponseStatusException badRequest(String reason) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, reason);
    }

    private String serializeAdditionalInformation(PaymentReceivedFrom paymentReceivedFrom,
                                                  TillsCreateAdditionalInformation additionalInformation) {
        Map<String, Object> storedInformation = new LinkedHashMap<>();
        storedInformation.put(PAYMENT_RECEIVED_FROM, paymentReceivedFrom.getCode());

        if (additionalInformation != null) {
            storedInformation.put(ADDITIONAL_INFORMATION, toStoredAdditionalInformation(additionalInformation));
        }

        try {
            return objectMapper.writeValueAsString(storedInformation);
        } catch (JacksonException e) {
            throw new IllegalArgumentException("Unable to store payment additional information", e);
        }
    }

    private Map<String, Object> toStoredAdditionalInformation(
        TillsCreateAdditionalInformation additionalInformation) {
        Map<String, Object> storedAdditionalInformation = objectMapper.convertValue(
            additionalInformation, new TypeReference<>() {});

        if (additionalInformation.getTransferSource() != null) {
            storedAdditionalInformation.put(TRANSFER_SOURCE,
                createTillMapper.toTransferSource(additionalInformation.getTransferSource()).getCode());
        } else {
            storedAdditionalInformation.remove(TRANSFER_SOURCE);
        }
        return storedAdditionalInformation;
    }

    private record TillCreationContext(BusinessUnitEntity businessUnit, UserStateV2 userState,
                                       LocalDateTime createdDate) {
    }
}
