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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.opal.dto.search.CourtSearchDto;
import uk.gov.hmcts.opal.entity.CourtEntity;
import uk.gov.hmcts.opal.service.CourtServiceInterface;

import java.util.List;

import static uk.gov.hmcts.opal.util.HttpUtil.buildResponse;


@RestController
@RequestMapping("/api/court")
@Slf4j(topic = "CourtController")
@Tag(name = "Court Controller")
public class CourtController {

    private final CourtServiceInterface courtService;

    public CourtController(@Qualifier("courtServiceProxy") CourtServiceInterface courtService) {
        this.courtService = courtService;
    }

    @GetMapping(value = "/{courtId}")
    @Operation(summary = "Returns the court for the given courtId.")
    public ResponseEntity<CourtEntity> getCourtById(@PathVariable Long courtId) {

        log.info(":GET:getCourtById: courtId: {}", courtId);

        CourtEntity response = courtService.getCourt(courtId);

        return buildResponse(response);
    }

    @PostMapping(value = "/search", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Searches courts based upon criteria in request body")
    public ResponseEntity<List<CourtEntity>> postCourtsSearch(@RequestBody CourtSearchDto criteria) {
        log.info(":POST:postCourtsSearch: query: \n{}", criteria);

        List<CourtEntity> response = courtService.searchCourts(criteria);

        return buildResponse(response);
    }


}
