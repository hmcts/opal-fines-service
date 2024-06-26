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
import uk.gov.hmcts.opal.dto.search.CreditorTransactionSearchDto;
import uk.gov.hmcts.opal.entity.CreditorTransactionEntity;
import uk.gov.hmcts.opal.service.CreditorTransactionServiceInterface;

import java.util.List;

import static uk.gov.hmcts.opal.util.HttpUtil.buildResponse;


@RestController
@RequestMapping("/api/creditor-transaction")
@Slf4j(topic = "CreditorTransactionController")
@Tag(name = "Creditor Transaction Controller")
public class CreditorTransactionController {

    private final CreditorTransactionServiceInterface creditorTransactionService;

    public CreditorTransactionController(@Qualifier("creditorTransactionServiceProxy")
                                         CreditorTransactionServiceInterface creditorTransactionService) {
        this.creditorTransactionService = creditorTransactionService;
    }

    @GetMapping(value = "/{creditorTransactionId}")
    @Operation(summary = "Returns the CreditorTransaction for the given creditorTransactionId.")
    public ResponseEntity<CreditorTransactionEntity> getCreditorTransactionById(@PathVariable
                                                                                    Long creditorTransactionId) {

        log.info(":GET:getCreditorTransactionById: creditorTransactionId: {}", creditorTransactionId);

        CreditorTransactionEntity response = creditorTransactionService.getCreditorTransaction(creditorTransactionId);

        return buildResponse(response);
    }

    @PostMapping(value = "/search", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Searches CreditorTransactions based upon criteria in request body")
    public ResponseEntity<List<CreditorTransactionEntity>> postCreditorTransactionsSearch(
        @RequestBody CreditorTransactionSearchDto criteria) {
        log.info(":POST:postCreditorTransactionsSearch: query: \n{}", criteria);

        List<CreditorTransactionEntity> response = creditorTransactionService.searchCreditorTransactions(criteria);

        return buildResponse(response);
    }


}
