package uk.gov.hmcts.opal.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
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
import uk.gov.hmcts.opal.dto.reference.CourtReferenceData;
import uk.gov.hmcts.opal.dto.reference.CourtReferenceDataResults;
import uk.gov.hmcts.opal.dto.search.CourtSearchDto;
import uk.gov.hmcts.opal.entity.court.CourtEntity;
import uk.gov.hmcts.opal.service.opal.CourtService;
import uk.gov.hmcts.opal.service.UserStateService;
import uk.gov.hmcts.opal.dto.response.SearchDataResponse;
import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.opal.util.HttpUtil.buildResponse;


@RestController
@RequestMapping("/courts")
@Slf4j(topic = "opal.CourtController")
@Tag(name = "Court Controller")
public class CourtController {

    private final CourtService courtService;

    private final UserStateService userStateService;

    public CourtController(UserStateService userStateService, CourtService courtService) {
        this.userStateService = userStateService;
        this.courtService = courtService;
    }

    @GetMapping(value = "/{courtId}")
    @Operation(summary = "Returns the court for the given courtId.")
    public ResponseEntity<CourtEntity.Lite> getCourtById(
        @PathVariable Long courtId, @RequestHeader(value = "Authorization", required = false) String authHeaderValue) {

        log.debug(":GET:getCourtById: courtId: {}", courtId);

        userStateService.checkForAuthorisedUser(authHeaderValue);

        CourtEntity.Lite response = courtService.getCourtById(courtId);

        return buildResponse(response);
    }

    @PostMapping(value = "/search", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Searches courts based upon criteria in request body")
    public ResponseEntity<SearchDataResponse<CourtEntity.Lite>> postCourtsSearch(
        @RequestBody CourtSearchDto criteria,
        @RequestHeader(value = "Authorization", required = false) String authHeaderValue) {

        log.debug(":POST:postCourtsSearch: query: \n{}", criteria);

        userStateService.checkForAuthorisedUser(authHeaderValue);

        List<CourtEntity.Lite> results = courtService.searchCourts(criteria);

        return ResponseEntity.ok(SearchDataResponse.<CourtEntity.Lite>builder()
            .searchData(results)
            .build());
    }

    @GetMapping
    @Operation(summary = "Returns courts as reference data with an optional filter applied")
    public ResponseEntity<CourtReferenceDataResults> getCourtRefData(
        @RequestParam("q") Optional<String> filter, @RequestParam("business_unit") Optional<Short> businessUnit) {
        log.debug(":GET:getCourtRefData: business unit: {}, filter string: {}", businessUnit, filter);

        List<CourtReferenceData> refData = courtService.getReferenceData(filter, businessUnit);

        log.debug(":GET:getCourtRefData: court reference data count: {}", refData.size());
        return ResponseEntity.ok(CourtReferenceDataResults.builder().refData(refData).build());
    }
}
