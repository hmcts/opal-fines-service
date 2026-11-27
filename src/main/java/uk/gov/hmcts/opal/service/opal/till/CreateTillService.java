package uk.gov.hmcts.opal.service.opal.till;

import static uk.gov.hmcts.opal.common.util.SecurityUtil.getOpalJwtAuthenticationTokenForCurrentUser;

import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.openapitools.jackson.nullable.JsonNullable;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.common.user.authorisation.exception.PermissionNotAllowedException;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserStateV2;
import uk.gov.hmcts.opal.entity.AssociatedRecordType;
import uk.gov.hmcts.opal.entity.MiscellaneousAccountEntity;
import uk.gov.hmcts.opal.entity.PaymentInEntity;
import uk.gov.hmcts.opal.entity.PaymentReceivedFrom;
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.entity.TillEntity;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;
import uk.gov.hmcts.opal.generated.model.TillsCreatePayerType;
import uk.gov.hmcts.opal.generated.model.TillsCreateAdditionalInformation;
import uk.gov.hmcts.opal.generated.model.TillsCreatePayerDetails;
import uk.gov.hmcts.opal.generated.model.TillsCreatePaymentDetails;
import uk.gov.hmcts.opal.generated.model.TillsCreatePaymentIn;
import uk.gov.hmcts.opal.generated.model.TillsCreateRequest;
import uk.gov.hmcts.opal.mapper.till.CreateTillMapper;
import uk.gov.hmcts.opal.repository.BusinessUnitRepository;
import uk.gov.hmcts.opal.repository.MiscellaneousAccountRepository;
import uk.gov.hmcts.opal.repository.PaymentInRepository;
import uk.gov.hmcts.opal.repository.PartyRepository;
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
    private final PartyRepository partyRepository;
    private final MiscellaneousAccountRepository miscellaneousAccountRepository;
    private final PreAllocatedCashTillService preAllocatedCashTillService;
    private final UserStateService userStateService;
    private final CreateTillMapper createTillMapper;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    private static final String PAYMENT_RECEIVED_FROM = "payment_received_from";
    private static final String ADDITIONAL_INFORMATION = "additional_information";
    private static final String TRANSFER_SOURCE = "transfer_source";
    private static final String PAYER_TYPE = "payer_type";
    private static final String PAYER_DETAILS = "payer_details";

    @Transactional
    public void createTill(TillsCreateRequest request) {
        Short businessUnitId = request.getBusinessUnitId();
        checkPermission(businessUnitId);
        List<TillsCreatePaymentIn> requestPayments = request.getPaymentsIn();
        Short paymentsCount = validatePaymentCount(requestPayments);
        validatePaymentScope(requestPayments, request.getAdditionalInformation());

        TillCreationContext context = resolveCreationContext(businessUnitId);
        TillEntity till = saveTill(requestPayments, paymentsCount, context);
        Long miscellaneousAccountId = createDurablePayerRecordsIfRequired(
            requestPayments, request.getAdditionalInformation(), context.businessUnit());

        savePayments(requestPayments, till, context.createdDate(), request.getAdditionalInformation(),
            miscellaneousAccountId);
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
                              LocalDateTime paymentDate, TillsCreateAdditionalInformation additionalInformation,
                              Long miscellaneousAccountId) {
        paymentInRepository.saveAll(requestPayments.stream()
            .map(payment -> toPaymentIn(payment, till, paymentDate, additionalInformation, miscellaneousAccountId))
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
        LocalDateTime dateTimeNow, TillsCreateAdditionalInformation additionalInformation,
        Long miscellaneousAccountId) {
        TillsCreatePaymentDetails details = requestPayment.getPaymentDetails();
        PaymentAssociation association = paymentAssociation(requestPayment, miscellaneousAccountId);

        return createTillMapper.toPaymentIn(
            requestPayment,
            till,
            dateTimeNow,
            serializeAdditionalInformation(
                createTillMapper.toPaymentReceivedFrom(details.getAdditionalInformation()),
                additionalInformation),
            association.associatedRecordType(),
            association.associatedRecordId());
    }

    private static void validatePaymentScope(List<TillsCreatePaymentIn> requestPayments,
                                             TillsCreateAdditionalInformation additionalInformation) {
        requestPayments.forEach(requestPayment -> validatePayment(requestPayment, additionalInformation));
    }

    private static Short validatePaymentCount(List<TillsCreatePaymentIn> requestPayments) {
        int paymentsCount = requestPayments.size();

        if (paymentsCount > Short.MAX_VALUE) {
            throw badRequest("A till cannot contain more than " + Short.MAX_VALUE + " payments");
        }
        return (short) paymentsCount;
    }

    private static void validatePayment(TillsCreatePaymentIn requestPayment,
                                        TillsCreateAdditionalInformation additionalInformation) {
        TillsCreatePaymentDetails.DestinationTypeEnum destinationType =
            requestPayment.getPaymentDetails().getDestinationType();

        switch (destinationType) {
            case FINES -> requireDefendantAccountId(requestPayment);
            case COURT_FEE -> {
                // Court fee payments are now in scope for manual till creation.
            }
            case SUSPENSE -> validateSuspenseReason(additionalInformation);
            default -> throw badRequest("Unsupported destination_type: " + destinationType);
        }
    }

    private static void validateSuspenseReason(TillsCreateAdditionalInformation additionalInformation) {
        TillsCreateAdditionalInformation.SuspenseReasonCodeEnum suspenseReasonCode =
            additionalInformation == null ? null : additionalInformation.getSuspenseReasonCode();

        if (suspenseReasonCode != TillsCreateAdditionalInformation.SuspenseReasonCodeEnum.FINES_OVERPAYMENT
            && suspenseReasonCode != TillsCreateAdditionalInformation.SuspenseReasonCodeEnum.FINE_PAID_IN_ADVANCE
            && suspenseReasonCode != TillsCreateAdditionalInformation.SuspenseReasonCodeEnum.MISCELLANEOUS_PAYMENT) {
            throw badRequest("Only suspense payments with reason OF, FA or MS are supported when creating a till");
        }
    }

    private static Long requireDefendantAccountId(TillsCreatePaymentIn requestPayment) {
        Long defendantAccountId = requestPayment.getDefendantAccountId();

        if (defendantAccountId == null) {
            throw badRequest("defendant_account_id is required for fines payments");
        }
        return defendantAccountId;
    }

    private static PaymentAssociation paymentAssociation(TillsCreatePaymentIn requestPayment,
                                                         Long miscellaneousAccountId) {
        if (requestPayment.getPaymentDetails().getDestinationType()
            == TillsCreatePaymentDetails.DestinationTypeEnum.FINES) {
            return new PaymentAssociation(AssociatedRecordType.DEFENDANT_ACCOUNTS,
                requireDefendantAccountId(requestPayment).toString());
        }

        if (miscellaneousAccountId == null) {
            throw new IllegalStateException("Miscellaneous account id is required for court fee and suspense payments");
        }
        return new PaymentAssociation(AssociatedRecordType.MISCELLANEOUS_ACCOUNTS,
            miscellaneousAccountId.toString());
    }

    private Long createDurablePayerRecordsIfRequired(List<TillsCreatePaymentIn> requestPayments,
                                                     TillsCreateAdditionalInformation additionalInformation,
                                                     BusinessUnitEntity businessUnit) {
        if (!containsDurablePayerPayment(requestPayments)) {
            return null;
        }

        PartyEntity party = partyRepository.save(toPartyEntity(additionalInformation));
        MiscellaneousAccountEntity miscellaneousAccount = miscellaneousAccountRepository.save(
            MiscellaneousAccountEntity.builder()
            .businessUnit(businessUnit)
            .party(party)
            .accountNumber(nextMiscellaneousAccountNumber(businessUnit.getBusinessUnitId()))
            .build());

        return Optional.ofNullable(miscellaneousAccount.getMiscellaneousAccountId())
            .orElseThrow(() -> new IllegalStateException("Miscellaneous account id was not generated"));
    }

    private boolean containsDurablePayerPayment(List<TillsCreatePaymentIn> requestPayments) {
        return requestPayments.stream().anyMatch(requestPayment -> {
            TillsCreatePaymentDetails.DestinationTypeEnum destinationType =
                requestPayment.getPaymentDetails().getDestinationType();
            return destinationType == TillsCreatePaymentDetails.DestinationTypeEnum.COURT_FEE
                || destinationType == TillsCreatePaymentDetails.DestinationTypeEnum.SUSPENSE;
        });
    }

    private PartyEntity toPartyEntity(TillsCreateAdditionalInformation additionalInformation) {
        if (additionalInformation == null) {
            throw badRequest("additional_information is required for court fee and qualifying suspense payments");
        }

        TillsCreatePayerType payerType = additionalInformation.getPayerType();
        if (payerType == null) {
            throw badRequest("payer_type is required for court fee and qualifying suspense payments");
        }

        TillsCreatePayerDetails payerDetails = requirePayerDetails(additionalInformation);

        return switch (payerType) {
            case INDIVIDUAL -> {
                validateIndividualPayerDetails(payerDetails);
                yield createTillMapper.toIndividualParty(payerDetails);
            }
            case ORGANISATION -> {
                validateOrganisationPayerDetails(payerDetails);
                yield createTillMapper.toOrganisationParty(payerDetails);
            }
        };
    }

    private TillsCreatePayerDetails requirePayerDetails(TillsCreateAdditionalInformation additionalInformation) {
        JsonNullable<TillsCreatePayerDetails> payerDetails = additionalInformation.getPayerDetails();
        if (payerDetails == null || !payerDetails.isPresent() || payerDetails.get() == null) {
            throw badRequest("additional_information.payer_details is required");
        }
        return payerDetails.get();
    }

    private void validateIndividualPayerDetails(TillsCreatePayerDetails payerDetails) {
        if (payerDetails.getIndividual() == null) {
            throw badRequest("additional_information.payer_details.individual is required for individual payer_type");
        }

        requiredText(payerDetails.getIndividual().getForenames(),
            "additional_information.payer_details.individual.forenames");
        requiredText(payerDetails.getIndividual().getSurname(),
            "additional_information.payer_details.individual.surname");
    }

    private void validateOrganisationPayerDetails(TillsCreatePayerDetails payerDetails) {
        if (payerDetails.getOrganisation() == null) {
            throw badRequest(
                "additional_information.payer_details.organisation is required for organisation payer_type");
        }

        requiredText(payerDetails.getOrganisation().getOrganisationName(),
            "additional_information.payer_details.organisation.organisation_name");
    }

    private static String requiredText(String value, String fieldPath) {
        value = text(value);
        if (value == null) {
            throw badRequest(fieldPath + " is required");
        }
        return value;
    }

    private static String text(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String nextMiscellaneousAccountNumber(Short businessUnitId) {
        return Optional.ofNullable(miscellaneousAccountRepository.getNextAccountNumber(businessUnitId))
            .filter(accountNumber -> !accountNumber.isBlank())
            .orElseThrow(() -> new IllegalStateException(
                "Miscellaneous account number was not generated for business unit " + businessUnitId));
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

        // Payer identity now lives in durable Party/MiscellaneousAccount records, not Payment In JSON.
        storedAdditionalInformation.remove(PAYER_TYPE);
        storedAdditionalInformation.remove(PAYER_DETAILS);

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

    private record PaymentAssociation(AssociatedRecordType associatedRecordType, String associatedRecordId) {
    }
}
