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
import uk.gov.hmcts.opal.dto.search.SuspenseAccountSearchDto;
import uk.gov.hmcts.opal.entity.SuspenseAccountEntity;
import uk.gov.hmcts.opal.service.SuspenseAccountServiceInterface;

import java.util.List;

import static uk.gov.hmcts.opal.util.HttpUtil.buildResponse;


@RestController
@RequestMapping("/dev/suspense-accounts")
@Slf4j(topic = "SuspenseAccountController")
@Tag(name = "Suspense Account Controller")
public class SuspenseAccountController {

    private final SuspenseAccountServiceInterface suspenseAccountService;

    public SuspenseAccountController(@Qualifier("suspenseAccountServiceProxy")
                                     SuspenseAccountServiceInterface suspenseAccountService) {
        this.suspenseAccountService = suspenseAccountService;
    }

    @GetMapping(value = "/{suspenseAccountId}")
    @Operation(summary = "Returns the SuspenseAccount for the given suspenseAccountId.")
    public ResponseEntity<SuspenseAccountEntity> getSuspenseAccountById(@PathVariable Long suspenseAccountId) {

        log.info(":GET:getSuspenseAccountById: suspenseAccountId: {}", suspenseAccountId);

        SuspenseAccountEntity response = suspenseAccountService.getSuspenseAccount(suspenseAccountId);

        return buildResponse(response);
    }

    @PostMapping(value = "/search", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Searches SuspenseAccounts based upon criteria in request body")
    public ResponseEntity<List<SuspenseAccountEntity>> postSuspenseAccountsSearch(
        @RequestBody SuspenseAccountSearchDto criteria) {
        log.info(":POST:postSuspenseAccountsSearch: query: \n{}", criteria);

        List<SuspenseAccountEntity> response = suspenseAccountService.searchSuspenseAccounts(criteria);

        return buildResponse(response);
    }


}
