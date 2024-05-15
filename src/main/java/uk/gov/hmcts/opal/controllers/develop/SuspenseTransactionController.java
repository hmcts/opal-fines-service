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
import uk.gov.hmcts.opal.dto.search.SuspenseTransactionSearchDto;
import uk.gov.hmcts.opal.entity.SuspenseTransactionEntity;
import uk.gov.hmcts.opal.service.SuspenseTransactionServiceInterface;

import java.util.List;

import static uk.gov.hmcts.opal.util.HttpUtil.buildResponse;


@RestController
@RequestMapping("/api/suspense-transaction")
@Slf4j(topic = "SuspenseTransactionController")
@Tag(name = "Suspense Transaction Controller")
public class SuspenseTransactionController {

    private final SuspenseTransactionServiceInterface suspenseTransactionService;

    public SuspenseTransactionController(@Qualifier("suspenseTransactionService")
                                         SuspenseTransactionServiceInterface suspenseTransactionService) {
        this.suspenseTransactionService = suspenseTransactionService;
    }

    @GetMapping(value = "/{suspenseTransactionId}")
    @Operation(summary = "Returns the SuspenseTransaction for the given suspenseTransactionId.")
    public ResponseEntity<SuspenseTransactionEntity> getSuspenseTransactionById(
        @PathVariable Long suspenseTransactionId) {

        log.info(":GET:getSuspenseTransactionById: suspenseTransactionId: {}", suspenseTransactionId);

        SuspenseTransactionEntity response = suspenseTransactionService.getSuspenseTransaction(suspenseTransactionId);

        return buildResponse(response);
    }

    @PostMapping(value = "/search", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Searches SuspenseTransactions based upon criteria in request body")
    public ResponseEntity<List<SuspenseTransactionEntity>> postSuspenseTransactionsSearch(
        @RequestBody SuspenseTransactionSearchDto criteria) {
        log.info(":POST:postSuspenseTransactionsSearch: query: \n{}", criteria);

        List<SuspenseTransactionEntity> response = suspenseTransactionService.searchSuspenseTransactions(criteria);

        return buildResponse(response);
    }


}
