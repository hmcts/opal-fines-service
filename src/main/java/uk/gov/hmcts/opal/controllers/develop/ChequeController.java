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
import uk.gov.hmcts.opal.dto.search.ChequeSearchDto;
import uk.gov.hmcts.opal.entity.ChequeEntity;
import uk.gov.hmcts.opal.service.ChequeServiceInterface;

import java.util.List;

import static uk.gov.hmcts.opal.util.HttpUtil.buildResponse;


@RestController
@RequestMapping("/dev/cheque")
@Slf4j(topic = "ChequeController")
@Tag(name = "Cheque Controller")
public class ChequeController {

    private final ChequeServiceInterface chequeService;

    public ChequeController(@Qualifier("chequeService") ChequeServiceInterface chequeService) {
        this.chequeService = chequeService;
    }

    @GetMapping(value = "/{chequeId}")
    @Operation(summary = "Returns the Cheque for the given chequeId.")
    public ResponseEntity<ChequeEntity> getChequeById(@PathVariable Long chequeId) {

        log.info(":GET:getChequeById: chequeId: {}", chequeId);

        ChequeEntity response = chequeService.getCheque(chequeId);

        return buildResponse(response);
    }

    @PostMapping(value = "/search", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Searches Cheques based upon criteria in request body")
    public ResponseEntity<List<ChequeEntity>> postChequesSearch(@RequestBody ChequeSearchDto criteria) {
        log.info(":POST:postChequesSearch: query: \n{}", criteria);

        List<ChequeEntity> response = chequeService.searchCheques(criteria);

        return buildResponse(response);
    }


}
