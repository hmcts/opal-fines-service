package uk.gov.hmcts.opal.controllers;

import io.swagger.v3.oas.annotations.Operation;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.opal.dto.AccountEnquiryDto;
import uk.gov.hmcts.opal.entity.DefendantAccountEntity;
import uk.gov.hmcts.opal.service.DefendantAccountService;

@RestController
@RequestMapping("/api/defendant-account")
@Data
@Slf4j
public class DefendantAccountController {

    @Autowired
    DefendantAccountService defendantAccountService;

    @GetMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Searches for a defendant account in the Opal DB")
    public ResponseEntity<DefendantAccountEntity> getDefendantAccount(@RequestBody AccountEnquiryDto request) {

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
}
