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
import uk.gov.hmcts.opal.dto.search.DefendantTransactionSearchDto;
import uk.gov.hmcts.opal.entity.DefendantTransactionEntity;
import uk.gov.hmcts.opal.service.DefendantTransactionServiceInterface;

import java.util.List;

import static uk.gov.hmcts.opal.util.HttpUtil.buildResponse;


@RestController
@RequestMapping("/dev/defendant-transactions")
@Slf4j(topic = "DefendantTransactionController")
@Tag(name = "DefendantTransaction Controller")
public class DefendantTransactionController {

    private final DefendantTransactionServiceInterface defendantTransactionService;

    public DefendantTransactionController(@Qualifier("defendantTransactionServiceProxy")
                                          DefendantTransactionServiceInterface defendantTransactionService) {
        this.defendantTransactionService = defendantTransactionService;
    }

    @GetMapping(value = "/{defendantTransactionId}")
    @Operation(summary = "Returns the DefendantTransaction for the given defendantTransactionId.")
    public ResponseEntity<DefendantTransactionEntity> getDefendantTransactionById(
        @PathVariable Long defendantTransactionId) {

        log.debug(":GET:getDefendantTransactionById: defendantTransactionId: {}", defendantTransactionId);

        DefendantTransactionEntity response = defendantTransactionService
            .getDefendantTransaction(defendantTransactionId);

        return buildResponse(response);
    }

    @PostMapping(value = "/search", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Searches DefendantTransactions based upon criteria in request body")
    public ResponseEntity<List<DefendantTransactionEntity>> postDefendantTransactionsSearch(
        @RequestBody DefendantTransactionSearchDto criteria) {
        log.debug(":POST:postDefendantTransactionsSearch: query: \n{}", criteria);

        List<DefendantTransactionEntity> response = defendantTransactionService.searchDefendantTransactions(criteria);

        log.debug(":POST:postDefendantTransactionsSearch: results: \n{}", response.size());

        return buildResponse(response);
    }


}
