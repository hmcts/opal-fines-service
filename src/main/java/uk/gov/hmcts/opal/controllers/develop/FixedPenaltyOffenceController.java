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
import uk.gov.hmcts.opal.dto.search.FixedPenaltyOffenceSearchDto;
import uk.gov.hmcts.opal.entity.FixedPenaltyOffenceEntity;
import uk.gov.hmcts.opal.service.FixedPenaltyOffenceServiceInterface;

import java.util.List;

import static uk.gov.hmcts.opal.util.HttpUtil.buildResponse;


@RestController
@RequestMapping("/dev/fixed-penalty-offences")
@Slf4j(topic = "FixedPenaltyOffenceController")
@Tag(name = "FixedPenaltyOffence Controller")
public class FixedPenaltyOffenceController {

    private final FixedPenaltyOffenceServiceInterface fixedPenaltyOffenceService;

    public FixedPenaltyOffenceController(
        @Qualifier("fixedPenaltyOffenceServiceProxy") FixedPenaltyOffenceServiceInterface fixedPenaltyOffenceService) {
        this.fixedPenaltyOffenceService = fixedPenaltyOffenceService;
    }

    @GetMapping(value = "/{fixedPenaltyOffenceId}")
    @Operation(summary = "Returns the FixedPenaltyOffence for the given fixedPenaltyOffenceId.")
    public ResponseEntity<FixedPenaltyOffenceEntity> getFixedPenaltyOffenceById(
        @PathVariable Long fixedPenaltyOffenceId) {

        log.info(":GET:getFixedPenaltyOffenceById: fixedPenaltyOffenceId: {}", fixedPenaltyOffenceId);

        FixedPenaltyOffenceEntity response = fixedPenaltyOffenceService.getFixedPenaltyOffence(fixedPenaltyOffenceId);

        return buildResponse(response);
    }

    @PostMapping(value = "/search", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Searches FixedPenaltyOffences based upon criteria in request body")
    public ResponseEntity<List<FixedPenaltyOffenceEntity>> postFixedPenaltyOffencesSearch(
        @RequestBody FixedPenaltyOffenceSearchDto criteria) {
        log.info(":POST:postFixedPenaltyOffencesSearch: query: \n{}", criteria);

        List<FixedPenaltyOffenceEntity> response = fixedPenaltyOffenceService.searchFixedPenaltyOffences(criteria);

        return buildResponse(response);
    }


}
