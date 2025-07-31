package uk.gov.hmcts.opal.disco.controllers;

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
import uk.gov.hmcts.opal.dto.search.BacsPaymentSearchDto;
import uk.gov.hmcts.opal.entity.BacsPaymentEntity;
import uk.gov.hmcts.opal.disco.BacsPaymentServiceInterface;

import java.util.List;

import static uk.gov.hmcts.opal.util.HttpUtil.buildResponse;


@RestController
@RequestMapping("/dev/bacs-payments")
@Slf4j(topic = "BacsPaymentController")
@Tag(name = "BacsPayment Controller")
public class BacsPaymentController {

    private final BacsPaymentServiceInterface bacsPaymentService;

    public BacsPaymentController(@Qualifier("bacsPaymentService") BacsPaymentServiceInterface bacsPaymentService) {
        this.bacsPaymentService = bacsPaymentService;
    }

    @GetMapping(value = "/{bacsPaymentId}")
    @Operation(summary = "Returns the BacsPayment for the given bacsPaymentId.")
    public ResponseEntity<BacsPaymentEntity> getBacsPaymentById(@PathVariable Long bacsPaymentId) {

        log.debug(":GET:getBacsPaymentById: bacsPaymentId: {}", bacsPaymentId);

        BacsPaymentEntity response = bacsPaymentService.getBacsPayment(bacsPaymentId);

        return buildResponse(response);
    }

    @PostMapping(value = "/search", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Searches BACS Payments based upon criteria in request body")
    public ResponseEntity<List<BacsPaymentEntity>> postBacsPaymentsSearch(@RequestBody BacsPaymentSearchDto criteria) {
        log.debug(":POST:postBacsPaymentsSearch: query: \n{}", criteria);

        List<BacsPaymentEntity> response = bacsPaymentService.searchBacsPayments(criteria);

        return buildResponse(response);
    }


}
