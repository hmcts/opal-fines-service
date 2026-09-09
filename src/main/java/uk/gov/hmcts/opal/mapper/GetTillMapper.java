package uk.gov.hmcts.opal.mapper;

import static uk.gov.hmcts.opal.entity.DestinationType.C;
import static uk.gov.hmcts.opal.entity.DestinationType.F;
import static uk.gov.hmcts.opal.entity.DestinationType.S;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.entity.PaymentInEntity;
import uk.gov.hmcts.opal.entity.TillEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.AssociationType;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountPartiesEntity;
import uk.gov.hmcts.opal.generated.model.TillsDefendantDetail;
import uk.gov.hmcts.opal.generated.model.TillsGetResponse;
import uk.gov.hmcts.opal.generated.model.TillsIndividualNames;
import uk.gov.hmcts.opal.generated.model.TillsOrganisationNames;
import uk.gov.hmcts.opal.generated.model.TillsPartyDetails;
import uk.gov.hmcts.opal.generated.model.TillsPayerName;
import uk.gov.hmcts.opal.generated.model.TillsPaymentIn;
import uk.gov.hmcts.opal.generated.model.TillsPaymentInItem;

@Component
@RequiredArgsConstructor
public class GetTillMapper {

    private final ObjectMapper objectMapper;

    public TillsGetResponse toResponse(TillEntity till, List<PaymentInEntity> payments,
                                      Map<Long, DefendantAccountEntity> defendantAccounts) {

        List<TillsPaymentInItem> paymentItems = payments.stream()
            .map(payment -> toPaymentItem(payment, defendantAccounts))
            .toList();

        return TillsGetResponse.builder()
            .tillNumber(till.getTillNumber())
            .businessUnitId(till.getBusinessUnit().getBusinessUnitId())
            .createdBy(till.getOwnedByName())
            .createdDate(till.getCreatedDate())
            .paymentsIn(paymentItems)
            .build();
    }

    private TillsPaymentInItem toPaymentItem(PaymentInEntity payment,
                                             Map<Long, DefendantAccountEntity> defendantAccounts) {

        AdditionalInformation additionalInformation = parseAdditionalInformation(payment);
        TillsPaymentIn mappedPayment = TillsPaymentIn.builder()
            .paymentInId(payment.getPaymentInId())
            .associatedRecordId(payment.getAssociatedRecordId())
            .associatedRecordType(payment.getAssociatedRecordType() == null ? null
                : TillsPaymentIn.AssociatedRecordTypeEnum.fromValue(payment.getAssociatedRecordType().getLabel()))
            .amount(payment.getPaymentAmount())
            .date(payment.getPaymentDate())
            .method(payment.getPaymentMethod() == null ? null
                : TillsPaymentIn.MethodEnum.fromValue(payment.getPaymentMethod().name()))
            .destinationType(payment.getDestinationType() == null ? null
                : TillsPaymentIn.DestinationTypeEnum.fromValue(payment.getDestinationType().name()))
            .allocationType(payment.getAllocationType())
            .paymentReceivedFrom(TillsPaymentIn.PaymentReceivedFromEnum.fromValue(
                additionalInformation.paymentReceivedFrom()))
            .thirdPartyPayerName(payment.getThirdPartyPayerName())
            .receipt(payment.isReceipt())
            .defendantDetail(payment.getDestinationType() == F
                ? toDefendantDetail(payment, defendantAccounts) : null)
            .payerName(payment.getDestinationType() == S || payment.getDestinationType() == C
                ? toPayerName(additionalInformation.payerDetails()) : null)
            .build();

        return TillsPaymentInItem.builder().paymentIn(mappedPayment).build();
    }

    private static TillsDefendantDetail toDefendantDetail(
        PaymentInEntity payment, Map<Long, DefendantAccountEntity> defendantAccounts) {

        Long defendantAccountId = Long.valueOf(payment.getAssociatedRecordId());
        DefendantAccountEntity account = defendantAccounts.get(defendantAccountId);
        if (account == null) {
            throw new EntityNotFoundException("Defendant account not found for associated_record_id: "
                + defendantAccountId);
        }

        PartyEntity defendant = account.getParties().stream()
            .filter(party -> party.getAssociationType() == AssociationType.DEFENDANT)
            .map(DefendantAccountPartiesEntity::getParty)
            .findFirst()
            .orElseThrow(() -> new EntityNotFoundException(
                "Defendant party not found for defendant_account_id: " + defendantAccountId));

        return TillsDefendantDetail.builder()
            .defendantAccountNumber(account.getAccountNumber())
            .partyDetails(toPartyDetails(defendant))
            .build();
    }

    private static TillsPartyDetails toPartyDetails(PartyEntity party) {
        TillsPartyDetails.Builder builder = TillsPartyDetails.builder()
            .organisationFlag(party.isOrganisation());
        if (party.isOrganisation()) {
            return builder.organisationNames(TillsOrganisationNames.builder()
                .organisationName(party.getOrganisationName())
                .build()).build();
        }
        return builder.individualNames(TillsIndividualNames.builder()
            .forenames(party.getForenames())
            .surname(party.getSurname())
            .build()).build();
    }

    private static TillsPayerName toPayerName(JsonNode payerDetails) {
        if (payerDetails == null || payerDetails.isMissingNode() || payerDetails.isNull()) {
            return null;
        }

        JsonNode individual = payerDetails.path("individual");
        JsonNode organisation = payerDetails.path("organisation");
        String forenames = textValue(individual, "forenames");
        String surname = textValue(individual, "surname");
        String organisationName = textValue(organisation, "organisation_name");
        if (forenames == null && surname == null && organisationName == null) {
            return null;
        }

        return TillsPayerName.builder()
            .forenames(forenames)
            .surname(surname)
            .organisationName(organisationName)
            .build();
    }

    private AdditionalInformation parseAdditionalInformation(PaymentInEntity payment) {
        String value = payment.getAdditionalInformation();
        if (value != null && value.matches("[DAT]")) {
            return new AdditionalInformation(value, null);
        }
        if (value == null || value.isBlank()) {
            throw invalidAdditionalInformation(payment, null);
        }

        try {
            JsonNode root = objectMapper.readTree(value);
            JsonNode information = root.path("additional_information");
            JsonNode details = information.isObject() ? information : root;
            String paymentReceivedFrom = firstTextValue(
                root.path("payment_received_from"),
                information.isTextual() ? information : null,
                root.path("payment_details").path("additional_information"),
                details.path("payment_received_from"));
            if (paymentReceivedFrom == null || !paymentReceivedFrom.matches("[DAT]")) {
                throw invalidAdditionalInformation(payment, null);
            }
            return new AdditionalInformation(paymentReceivedFrom, details.path("payer_details"));
        } catch (JacksonException exception) {
            throw invalidAdditionalInformation(payment, exception);
        }
    }

    private static String firstTextValue(JsonNode... nodes) {
        for (JsonNode node : nodes) {
            if (node != null && node.isTextual() && !node.asText().isBlank()) {
                return node.asText();
            }
        }
        return null;
    }

    private static String textValue(JsonNode parent, String fieldName) {
        JsonNode value = parent.path(fieldName);
        return value.isTextual() && !value.asText().isBlank() ? value.asText() : null;
    }

    private static IllegalStateException invalidAdditionalInformation(
        PaymentInEntity payment, Exception cause) {

        return new IllegalStateException("Payment " + payment.getPaymentInId()
            + " has invalid additional_information", cause);
    }

    private record AdditionalInformation(String paymentReceivedFrom, JsonNode payerDetails) {
    }
}
