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
import uk.gov.hmcts.opal.dto.search.MisDebtorSearchDto;
import uk.gov.hmcts.opal.entity.MisDebtorEntity;
import uk.gov.hmcts.opal.service.MisDebtorServiceInterface;

import java.util.List;

import static uk.gov.hmcts.opal.util.HttpUtil.buildResponse;


@RestController
@RequestMapping("/api/mis-debtor")
@Slf4j(topic = "MisDebtorController")
@Tag(name = "MisDebtor Controller")
public class MisDebtorController {

    private final MisDebtorServiceInterface misDebtorService;

    public MisDebtorController(@Qualifier("misDebtorServiceProxy") MisDebtorServiceInterface misDebtorService) {
        this.misDebtorService = misDebtorService;
    }

    @GetMapping(value = "/{misDebtorId}")
    @Operation(summary = "Returns the MisDebtor for the given misDebtorId.")
    public ResponseEntity<MisDebtorEntity> getMisDebtorById(@PathVariable Long misDebtorId) {

        log.info(":GET:getMisDebtorById: misDebtorId: {}", misDebtorId);

        MisDebtorEntity response = misDebtorService.getMisDebtor(misDebtorId);

        return buildResponse(response);
    }

    @PostMapping(value = "/search", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Searches MisDebtors based upon criteria in request body")
    public ResponseEntity<List<MisDebtorEntity>> postMisDebtorsSearch(@RequestBody MisDebtorSearchDto criteria) {
        log.info(":POST:postMisDebtorsSearch: query: \n{}", criteria);

        List<MisDebtorEntity> response = misDebtorService.searchMisDebtors(criteria);

        return buildResponse(response);
    }


}
