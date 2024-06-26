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
import uk.gov.hmcts.opal.dto.search.MiscellaneousAccountSearchDto;
import uk.gov.hmcts.opal.entity.MiscellaneousAccountEntity;
import uk.gov.hmcts.opal.service.MiscellaneousAccountServiceInterface;

import java.util.List;

import static uk.gov.hmcts.opal.util.HttpUtil.buildResponse;


@RestController
@RequestMapping("/api/miscellaneous-account")
@Slf4j(topic = "MiscellaneousAccountController")
@Tag(name = "MiscellaneousAccount Controller")
public class MiscellaneousAccountController {

    private final MiscellaneousAccountServiceInterface miscellaneousAccountService;

    public MiscellaneousAccountController(@Qualifier("miscellaneousAccountServiceProxy")
                                          MiscellaneousAccountServiceInterface miscellaneousAccountService) {
        this.miscellaneousAccountService = miscellaneousAccountService;
    }

    @GetMapping(value = "/{miscellaneousAccountId}")
    @Operation(summary = "Returns the MiscellaneousAccount for the given miscellaneousAccountId.")
    public ResponseEntity<MiscellaneousAccountEntity> getMiscellaneousAccountById(@PathVariable
                                                                                      Long miscellaneousAccountId) {

        log.info(":GET:getMiscellaneousAccountById: miscellaneousAccountId: {}", miscellaneousAccountId);

        MiscellaneousAccountEntity response = miscellaneousAccountService
            .getMiscellaneousAccount(miscellaneousAccountId);

        return buildResponse(response);
    }

    @PostMapping(value = "/search", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Searches MiscellaneousAccounts based upon criteria in request body")
    public ResponseEntity<List<MiscellaneousAccountEntity>> postMiscellaneousAccountsSearch(
        @RequestBody MiscellaneousAccountSearchDto criteria) {

        log.info(":POST:postMiscellaneousAccountsSearch: query: \n{}", criteria);

        List<MiscellaneousAccountEntity> response = miscellaneousAccountService.searchMiscellaneousAccounts(criteria);

        return buildResponse(response);
    }


}
