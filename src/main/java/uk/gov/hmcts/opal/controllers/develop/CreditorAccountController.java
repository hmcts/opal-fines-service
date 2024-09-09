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
import uk.gov.hmcts.opal.dto.search.CreditorAccountSearchDto;
import uk.gov.hmcts.opal.entity.CreditorAccountEntity;
import uk.gov.hmcts.opal.service.CreditorAccountServiceInterface;

import java.util.List;

import static uk.gov.hmcts.opal.util.HttpUtil.buildResponse;


@RestController
@RequestMapping("/dev/creditor-accounts")
@Slf4j(topic = "CreditorAccountController")
@Tag(name = "Creditor Account Controller")
public class CreditorAccountController {

    private final CreditorAccountServiceInterface creditorAccountService;

    public CreditorAccountController(@Qualifier("creditorAccountServiceProxy")
                                     CreditorAccountServiceInterface creditorAccountService) {
        this.creditorAccountService = creditorAccountService;
    }

    @GetMapping(value = "/{creditorAccountId}")
    @Operation(summary = "Returns the CreditorAccount for the given creditorAccountId.")
    public ResponseEntity<CreditorAccountEntity> getCreditorAccountById(@PathVariable Long creditorAccountId) {

        log.info(":GET:getCreditorAccountById: creditorAccountId: {}", creditorAccountId);

        CreditorAccountEntity response = creditorAccountService.getCreditorAccount(creditorAccountId);

        return buildResponse(response);
    }

    @PostMapping(value = "/search", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Searches CreditorAccounts based upon criteria in request body")
    public ResponseEntity<List<CreditorAccountEntity>> postCreditorAccountsSearch(
        @RequestBody CreditorAccountSearchDto criteria) {
        log.info(":POST:postCreditorAccountsSearch: query: \n{}", criteria);

        List<CreditorAccountEntity> response = creditorAccountService.searchCreditorAccounts(criteria);

        return buildResponse(response);
    }


}
