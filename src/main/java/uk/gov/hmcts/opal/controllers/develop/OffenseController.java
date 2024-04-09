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
import uk.gov.hmcts.opal.dto.search.OffenseSearchDto;
import uk.gov.hmcts.opal.entity.OffenseEntity;
import uk.gov.hmcts.opal.service.OffenseServiceInterface;

import java.util.List;

import static uk.gov.hmcts.opal.util.HttpUtil.buildResponse;


@RestController
@RequestMapping("/api/offense")
@Slf4j(topic = "OffenseController")
@Tag(name = "Offense Controller")
public class OffenseController {

    private final OffenseServiceInterface offenseService;

    public OffenseController(@Qualifier("offenseService") OffenseServiceInterface offenseService) {
        this.offenseService = offenseService;
    }

    @GetMapping(value = "/{offenseId}")
    @Operation(summary = "Returns the Offense for the given offenseId.")
    public ResponseEntity<OffenseEntity> getOffenseById(@PathVariable Long offenseId) {

        log.info(":GET:getOffenseById: offenseId: {}", offenseId);

        OffenseEntity response = offenseService.getOffense(offenseId);

        return buildResponse(response);
    }

    @PostMapping(value = "/search", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Searches Offenses based upon criteria in request body")
    public ResponseEntity<List<OffenseEntity>> postOffensesSearch(@RequestBody OffenseSearchDto criteria) {
        log.info(":POST:postOffensesSearch: query: \n{}", criteria);

        List<OffenseEntity> response = offenseService.searchOffenses(criteria);

        return buildResponse(response);
    }


}
