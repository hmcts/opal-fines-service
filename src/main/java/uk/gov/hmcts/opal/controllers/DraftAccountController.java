package uk.gov.hmcts.opal.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.opal.dto.search.DraftAccountSearchDto;
import uk.gov.hmcts.opal.entity.DraftAccountEntity;
import uk.gov.hmcts.opal.service.opal.DraftAccountService;
import uk.gov.hmcts.opal.service.opal.UserStateService;

import java.util.List;

import static uk.gov.hmcts.opal.util.HttpUtil.buildResponse;


@RestController
@RequestMapping("/api/draft-account")
@Slf4j(topic = "DraftAccountController")
@Tag(name = "DraftAccount Controller")
public class DraftAccountController {

    private final DraftAccountService opalDraftAccountService;

    private final UserStateService userStateService;

    public DraftAccountController(UserStateService userStateService, DraftAccountService opalDraftAccountService) {
        this.opalDraftAccountService = opalDraftAccountService;
        this.userStateService = userStateService;
    }

    @GetMapping(value = "/{draftAccountId}")
    @Operation(summary = "Returns the Draft Account for the given draftAccountId.")
    public ResponseEntity<DraftAccountEntity> getDraftAccountById(@PathVariable Long draftAccountId,
                                                    @RequestHeader("Authorization") String authHeaderValue) {

        log.info(":GET:getDraftAccountById: draftAccountId: {}", draftAccountId);

        userStateService.checkForAuthorisedUser(authHeaderValue);

        DraftAccountEntity response = opalDraftAccountService.getDraftAccount(draftAccountId);

        return buildResponse(response);
    }

    @PostMapping(value = "/search", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Searches Draft Accounts based upon criteria in request body")
    public ResponseEntity<List<DraftAccountEntity>> postDraftAccountsSearch(@RequestBody DraftAccountSearchDto criteria,
                                                              @RequestHeader("Authorization") String authHeaderValue) {
        log.info(":POST:postDraftAccountsSearch: query: \n{}", criteria);

        userStateService.checkForAuthorisedUser(authHeaderValue);

        List<DraftAccountEntity> response = opalDraftAccountService.searchDraftAccounts(criteria);

        return buildResponse(response);
    }

}
