package uk.gov.hmcts.opal.service.opal.till;

import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;
import uk.gov.hmcts.opal.entity.CourtFeeEntity;
import uk.gov.hmcts.opal.entity.DestinationType;
import uk.gov.hmcts.opal.entity.PaymentInEntity;
import uk.gov.hmcts.opal.generated.model.TillsCreateAdditionalInformation;
import uk.gov.hmcts.opal.mapper.till.CreateTillMapper;
import uk.gov.hmcts.opal.repository.CourtFeeRepository;

@Service
@RequiredArgsConstructor
public class CreateTillCourtFeeService {

    private final CourtFeeRepository courtFeeRepository;
    private final CreateTillMapper mapper;
    private final ObjectMapper objectMapper;

    public List<PaymentInEntity> split(PaymentInEntity payment, TillsCreateAdditionalInformation information,
                                       Short businessUnitId) {
        if (payment.getDestinationType() != DestinationType.C) {
            return List.of(payment);
        }
        CourtFeeEntity fee = courtFeeRepository.findByBusinessUnit_BusinessUnitIdAndCourtFeeCode(
                businessUnitId, information.getCourtFeeCode())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "court_fee_code must identify an existing Court Fee in business_unit_id " + businessUnitId));
        if (fee.getAmount().signum() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Court Fee amount must be greater than zero");
        }
        BigDecimal feeAmount = fee.getAmount().multiply(BigDecimal.valueOf(information.getNumberOfItems()));
        if (payment.getPaymentAmount().compareTo(feeAmount) <= 0) {
            return List.of(payment);
        }

        PaymentInEntity overpayment = mapper.copyPayment(payment);
        overpayment.setPaymentAmount(payment.getPaymentAmount().subtract(feeAmount));
        payment.setPaymentAmount(feeAmount);
        markCourtFeePart(payment, false);
        markCourtFeePart(overpayment, true);
        return List.of(payment, overpayment);
    }

    private void markCourtFeePart(PaymentInEntity payment, boolean overpayment) {
        ObjectNode metadata = (ObjectNode) objectMapper.readTree(payment.getAdditionalInformation());
        // Internal allocation discriminator: clients continue to submit one Court Fee payment.
        metadata.put("court_fee_overpayment", overpayment);
        payment.setAdditionalInformation(objectMapper.writeValueAsString(metadata));
    }
}
