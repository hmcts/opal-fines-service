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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.opal.dto.reference.CourtReferenceDataResults;
import uk.gov.hmcts.opal.dto.search.CourtSearchDto;
import uk.gov.hmcts.opal.entity.CourtEntity;
import uk.gov.hmcts.opal.entity.projection.CourtReferenceData;
import uk.gov.hmcts.opal.service.CourtServiceInterface;
import uk.gov.hmcts.opal.service.opal.CourtService;
import uk.gov.hmcts.opal.service.opal.UserStateService;

import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.opal.util.HttpUtil.buildResponse;


@RestController
@RequestMapping("/api/court")
@Slf4j(topic = "CourtController")
@Tag(name = "Court Controller")
public class CourtController {

    private final CourtServiceInterface courtService;

    private final CourtService opalCourtService;

    private final UserStateService userStateService;

    public CourtController(@Qualifier("courtServiceProxy") CourtServiceInterface courtService,
                           UserStateService userStateService, CourtService opalCourtService) {
        this.courtService = courtService;
        this.userStateService = userStateService;
        this.opalCourtService = opalCourtService;
    }

    @GetMapping(value = "/{courtId}")
    @Operation(summary = "Returns the court for the given courtId.")
    public ResponseEntity<CourtEntity> getCourtById(@PathVariable Long courtId,
                                                    @RequestHeader("Authorization") String authHeaderValue) {

        log.info(":GET:getCourtById: courtId: {}", courtId);

        userStateService.checkForAuthorisedUser(authHeaderValue);

        CourtEntity response = courtService.getCourt(courtId);

        return buildResponse(response);
    }

    @PostMapping(value = "/search", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Searches courts based upon criteria in request body")
    public ResponseEntity<List<CourtEntity>> postCourtsSearch(@RequestBody CourtSearchDto criteria,
                                                              @RequestHeader("Authorization") String authHeaderValue) {
        log.info(":POST:postCourtsSearch: query: \n{}", criteria);

        userStateService.checkForAuthorisedUser(authHeaderValue);

        List<CourtEntity> response = courtService.searchCourts(criteria);

        return buildResponse(response);
    }

    @GetMapping(value = {"/ref-data", "/ref-data/", "/ref-data/{filter}"})
    @Operation(summary = "Returns courts as reference data with an optional filter applied")
    public ResponseEntity<CourtReferenceDataResults> getCourtRefData(
        @PathVariable Optional<String> filter, @RequestParam Optional<Short> businessUnit) {
        log.info(":GET:getCourtRefData: business unit: {}, filter string: {}", businessUnit, filter);

        List<CourtReferenceData> refData = opalCourtService.getReferenceData(filter, businessUnit);

        log.info(":GET:getCourtRefData: court reference data count: {}", refData.size());
        return ResponseEntity.ok(CourtReferenceDataResults.builder().refData(refData).build());
    }
}
