package uk.gov.hmcts.opal.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.opal.dto.search.PaymentInSearchDto;
import uk.gov.hmcts.opal.entity.PaymentInEntity;
import uk.gov.hmcts.opal.service.PaymentInServiceInterface;

import java.util.List;

import static uk.gov.hmcts.opal.util.ResponseUtil.buildResponse;


@RestController
@RequestMapping("/api/paymentIns")
@Slf4j(topic = "PaymentInController")
@Tag(name = "PaymentIn Controller")
public class PaymentInController {

    private final PaymentInServiceInterface paymentInService;

    public PaymentInController(@Qualifier("paymentInServiceProxy") PaymentInServiceInterface paymentInService) {
        this.paymentInService = paymentInService;
    }

    @GetMapping(value = "/{paymentInId}")
    @Operation(summary = "Returns the PaymentIn for the given paymentInId.")
    public ResponseEntity<PaymentInEntity> getPaymentInById(@PathVariable Long paymentInId) {

        log.info(":GET:getPaymentInById: paymentInId: {}", paymentInId);

        PaymentInEntity response = paymentInService.getPaymentIn(paymentInId);

        return buildResponse(response);
    }

    @PostMapping(value = "/search", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Searches PaymentIns based upon criteria in request body")
    public ResponseEntity<List<PaymentInEntity>> postPaymentInsSearch(@RequestBody PaymentInSearchDto criteria) {
        log.info(":POST:postPaymentInsSearch: query: \n{}", criteria);

        List<PaymentInEntity> response = paymentInService.searchPaymentIns(criteria);

        return buildResponse(response);
    }


}
