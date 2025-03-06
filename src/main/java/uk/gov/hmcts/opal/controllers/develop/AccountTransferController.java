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
import uk.gov.hmcts.opal.dto.search.AccountTransferSearchDto;
import uk.gov.hmcts.opal.entity.AccountTransferEntity;
import uk.gov.hmcts.opal.service.AccountTransferServiceInterface;

import java.util.List;

import static uk.gov.hmcts.opal.util.HttpUtil.buildResponse;


@RestController
@RequestMapping("/dev/account-transfers")
@Slf4j(topic = "AccountTransferController")
@Tag(name = "AccountTransfer Controller")
public class AccountTransferController {

    private final AccountTransferServiceInterface accountTransferService;

    public AccountTransferController(
        @Qualifier("accountTransferServiceProxy") AccountTransferServiceInterface accountTransferService) {
        this.accountTransferService = accountTransferService;
    }

    @GetMapping(value = "/{accountTransferId}")
    @Operation(summary = "Returns the AccountTransfer for the given accountTransferId.")
    public ResponseEntity<AccountTransferEntity> getAccountTransferById(@PathVariable Long accountTransferId) {

        log.debug(":GET:getAccountTransferById: accountTransferId: {}", accountTransferId);

        AccountTransferEntity response = accountTransferService.getAccountTransfer(accountTransferId);

        return buildResponse(response);
    }

    @PostMapping(value = "/search", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Searches AccountTransfers based upon criteria in request body")
    public ResponseEntity<List<AccountTransferEntity>> postAccountTransfersSearch(
        @RequestBody AccountTransferSearchDto criteria) {
        log.debug(":POST:postAccountTransfersSearch: query: \n{}", criteria);

        List<AccountTransferEntity> response = accountTransferService.searchAccountTransfers(criteria);

        return buildResponse(response);
    }


}
