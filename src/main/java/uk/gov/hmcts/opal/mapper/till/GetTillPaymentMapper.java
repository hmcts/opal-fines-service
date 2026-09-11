package uk.gov.hmcts.opal.mapper.till;

import static uk.gov.hmcts.opal.entity.DestinationType.C;
import static uk.gov.hmcts.opal.entity.DestinationType.S;

import java.util.Map;
import org.mapstruct.AfterMapping;
import org.mapstruct.Builder;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.openapitools.jackson.nullable.JsonNullable;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import uk.gov.hmcts.opal.entity.AssociatedRecordType;
import uk.gov.hmcts.opal.entity.PaymentInEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.generated.model.TillsPayerName;
import uk.gov.hmcts.opal.generated.model.TillsPaymentIn;
import uk.gov.hmcts.opal.generated.model.TillsPaymentInItem;

@Mapper(
    componentModel = "spring",
    uses = GetTillDefendantMapper.class,
    builder = @Builder(disableBuilder = true)
)
public abstract class GetTillPaymentMapper {

    private static final ObjectMapper OBJECT_MAPPER = JsonMapper.builder().build();

    @Mapping(target = "paymentIn", source = "payment")
    public abstract TillsPaymentInItem toPaymentItem(
        PaymentInEntity payment, @Context Map<Long, DefendantAccountEntity> defendantAccounts);

    @Mapping(target = "associatedRecordId", qualifiedByName = "toNullableString")
    @Mapping(target = "associatedRecordType", qualifiedByName = "toAssociatedRecordType")
    @Mapping(target = "amount", source = "paymentAmount")
    @Mapping(target = "date", source = "paymentDate")
    @Mapping(target = "method", source = "paymentMethod")
    @Mapping(target = "allocationType", qualifiedByName = "toNullableString")
    @Mapping(target = "paymentReceivedFrom", ignore = true)
    @Mapping(target = "thirdPartyPayerName", qualifiedByName = "toNullableString")
    @Mapping(target = "defendantDetail", source = "payment", qualifiedByName = "resolveDefendantDetail")
    @Mapping(target = "payerName", ignore = true)
    protected abstract TillsPaymentIn toPayment(
        PaymentInEntity payment, @Context Map<Long, DefendantAccountEntity> defendantAccounts);

    @AfterMapping
    protected void addPaymentDetails(PaymentInEntity payment, @MappingTarget TillsPaymentIn target) {
        AdditionalInformation additionalInformation = parseAdditionalInformation(payment);
        target.setPaymentReceivedFrom(TillsPaymentIn.PaymentReceivedFromEnum.fromValue(
            additionalInformation.paymentReceivedFrom()));
        target.setPayerName(resolvePayerName(payment, additionalInformation.payerDetails()));
    }

    @Named("toNullableString")
    protected JsonNullable<String> toNullableString(String value) {
        return JsonNullable.of(value);
    }

    @Named("toAssociatedRecordType")
    protected JsonNullable<TillsPaymentIn.AssociatedRecordTypeEnum> toAssociatedRecordType(
        AssociatedRecordType associatedRecordType) {

        return JsonNullable.of(associatedRecordType == null ? null
            : TillsPaymentIn.AssociatedRecordTypeEnum.fromValue(associatedRecordType.getLabel()));
    }

    protected JsonNullable<TillsPayerName> resolvePayerName(PaymentInEntity payment, JsonNode payerDetails) {
        if (payment.getDestinationType() != S && payment.getDestinationType() != C) {
            return JsonNullable.of(null);
        }
        if (payerDetails == null || payerDetails.isMissingNode() || payerDetails.isNull()) {
            return JsonNullable.of(null);
        }

        JsonNode individual = payerDetails.path("individual");
        JsonNode organisation = payerDetails.path("organisation");
        String forenames = textValue(individual, "forenames");
        String surname = textValue(individual, "surname");
        String organisationName = textValue(organisation, "organisation_name");
        if (forenames == null && surname == null && organisationName == null) {
            return JsonNullable.of(null);
        }

        return JsonNullable.of(toPayerName(forenames, surname, organisationName));
    }

    @Mapping(target = "forenames", source = "forenames", qualifiedByName = "toNullableString")
    @Mapping(target = "surname", source = "surname", qualifiedByName = "toNullableString")
    @Mapping(target = "organisationName", source = "organisationName", qualifiedByName = "toNullableString")
    protected abstract TillsPayerName toPayerName(String forenames, String surname, String organisationName);

    private AdditionalInformation parseAdditionalInformation(PaymentInEntity payment) {
        String value = payment.getAdditionalInformation();
        if (value != null && value.matches("[DAT]")) {
            return new AdditionalInformation(value, null);
        }
        if (value == null || value.isBlank()) {
            throw invalidAdditionalInformation(payment, null);
        }

        try {
            JsonNode root = OBJECT_MAPPER.readTree(value);
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

    protected record AdditionalInformation(String paymentReceivedFrom, JsonNode payerDetails) {

    }
}
