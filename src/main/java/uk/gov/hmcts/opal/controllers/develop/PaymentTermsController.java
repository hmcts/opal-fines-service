package uk.gov.hmcts.opal.controllers.develop;

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
import uk.gov.hmcts.opal.dto.search.PaymentTermsSearchDto;
import uk.gov.hmcts.opal.entity.PaymentTermsEntity;
import uk.gov.hmcts.opal.service.PaymentTermsServiceInterface;

import java.util.List;

import static uk.gov.hmcts.opal.util.HttpUtil.buildResponse;


@RestController
@RequestMapping("/api/payment-terms")
@Slf4j(topic = "PaymentTermsController")
@Tag(name = "PaymentTerms Controller")
public class PaymentTermsController {

    private final PaymentTermsServiceInterface paymentTermsService;

    public PaymentTermsController(
        @Qualifier("paymentTermsService") PaymentTermsServiceInterface paymentTermsService) {
        this.paymentTermsService = paymentTermsService;
    }

    @GetMapping(value = "/{paymentTermsId}")
    @Operation(summary = "Returns the PaymentTerms for the given paymentTermsId.")
    public ResponseEntity<PaymentTermsEntity> getPaymentTermsById(@PathVariable Long paymentTermsId) {

        log.info(":GET:getPaymentTermsById: paymentTermsId: {}", paymentTermsId);

        PaymentTermsEntity response = paymentTermsService.getPaymentTerms(paymentTermsId);

        return buildResponse(response);
    }

    @PostMapping(value = "/search", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Searches PaymentTerms based upon criteria in request body")
    public ResponseEntity<List<PaymentTermsEntity>> postPaymentTermsSearch(
        @RequestBody PaymentTermsSearchDto criteria) {
        log.info(":POST:postPaymentTermsSearch: query: \n{}", criteria);

        List<PaymentTermsEntity> response = paymentTermsService.searchPaymentTerms(criteria);

        return buildResponse(response);
    }


}
