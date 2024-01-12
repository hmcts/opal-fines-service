package uk.gov.hmcts.opal.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.opal.dto.AccountDetailsDto;
import uk.gov.hmcts.opal.dto.AccountEnquiryDto;
import uk.gov.hmcts.opal.dto.AccountSearchDto;
import uk.gov.hmcts.opal.dto.AccountSearchResultsDto;
import uk.gov.hmcts.opal.entity.DefendantAccountEntity;
import uk.gov.hmcts.opal.service.DefendantAccountServiceInterface;

import java.util.List;

@RestController
@RequestMapping("/api/defendant-account")
@Slf4j
@Tag(name = "Defendant Account Controller")
public class DefendantAccountController {

    private final DefendantAccountServiceInterface defendantAccountService;

    public DefendantAccountController(@Qualifier("defendantAccountServiceProxy")
                                      DefendantAccountServiceInterface defendantAccountService) {
        this.defendantAccountService = defendantAccountService;
    }

    @GetMapping
    @Operation(summary = "Searches for a defendant account in the Opal DB")
    public ResponseEntity<DefendantAccountEntity> getDefendantAccount(
        @RequestParam(name = "businessUnitId") Short businessUnitId,
        @RequestParam(name = "accountNumber") String accountNumber) {

        AccountEnquiryDto request = AccountEnquiryDto.builder()
            .businessUnitId(businessUnitId)
            .accountNumber(accountNumber)
            .build();

        DefendantAccountEntity response = defendantAccountService.getDefendantAccount(request);

        if (response == null) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(response);
    }

    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Updates defendant account information")
    public ResponseEntity<DefendantAccountEntity> putDefendantAccount(
        @RequestBody DefendantAccountEntity defendantAccountEntity) {

        DefendantAccountEntity response = defendantAccountService.putDefendantAccount(defendantAccountEntity);

        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/{businessUnit}")
    @Operation(summary = "Returns all defendant accounts within a business unit")
    public ResponseEntity<List<DefendantAccountEntity>> getDefendantAccountsByBusinessUnit(
        @PathVariable short businessUnit) {

        log.info(":GET:getDefendantAccountsByBusinessUnit: busUnit: {}", businessUnit);
        List<DefendantAccountEntity> response = defendantAccountService
            .getDefendantAccountsByBusinessUnit(businessUnit);

        if (response == null) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/details")
    @Operation(summary = "Get defendant account details by providing the defendant account summary")
    public ResponseEntity<AccountDetailsDto> getAccountDetailsByAccountSummary(
        @RequestParam(name = "defendantAccountId") Long defendantAccountId) {

        AccountDetailsDto response = defendantAccountService.getAccountDetailsByDefendantAccountId(defendantAccountId);

        if (response == null) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/search", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Searches defendant accounts based upon criteria in request body")
    public ResponseEntity<AccountSearchResultsDto> postDefendantAccountSearch(
        @RequestBody AccountSearchDto accountSearchDto) {

        AccountSearchResultsDto response = defendantAccountService.searchDefendantAccounts(accountSearchDto);
        return ResponseEntity.ok(response);
    }
}
