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
import uk.gov.hmcts.opal.dto.search.PrisonSearchDto;
import uk.gov.hmcts.opal.entity.PrisonEntity;
import uk.gov.hmcts.opal.service.PrisonServiceInterface;

import java.util.List;

import static uk.gov.hmcts.opal.util.HttpUtil.buildResponse;


@RestController
@RequestMapping("/dev/prisons")
@Slf4j(topic = "PrisonController")
@Tag(name = "Prison Controller")
public class PrisonController {

    private final PrisonServiceInterface prisonService;

    public PrisonController(@Qualifier("prisonServiceProxy") PrisonServiceInterface prisonService) {
        this.prisonService = prisonService;
    }

    @GetMapping(value = "/{prisonId}")
    @Operation(summary = "Returns the Prison for the given prisonId.")
    public ResponseEntity<PrisonEntity> getPrisonById(@PathVariable Long prisonId) {

        log.debug(":GET:getPrisonById: prisonId: {}", prisonId);

        PrisonEntity response = prisonService.getPrison(prisonId);

        return buildResponse(response);
    }

    @PostMapping(value = "/search", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Searches Prisons based upon criteria in request body")
    public ResponseEntity<List<PrisonEntity>> postPrisonsSearch(@RequestBody PrisonSearchDto criteria) {
        log.debug(":POST:postPrisonsSearch: query: \n{}", criteria);

        List<PrisonEntity> response = prisonService.searchPrisons(criteria);

        return buildResponse(response);
    }


}
