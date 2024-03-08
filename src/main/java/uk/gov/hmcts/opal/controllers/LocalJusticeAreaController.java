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
import uk.gov.hmcts.opal.dto.search.LocalJusticeAreaSearchDto;
import uk.gov.hmcts.opal.entity.LocalJusticeAreaEntity;
import uk.gov.hmcts.opal.service.LocalJusticeAreaServiceInterface;

import java.util.List;

import static uk.gov.hmcts.opal.util.HttpUtil.buildResponse;


@RestController
@RequestMapping("/api/local-justice-area")
@Slf4j(topic = "LocalJusticeAreaController")
@Tag(name = "LocalJusticeArea Controller")
public class LocalJusticeAreaController {

    private final LocalJusticeAreaServiceInterface localJusticeAreaService;

    public LocalJusticeAreaController(
        @Qualifier("localJusticeAreaServiceProxy") LocalJusticeAreaServiceInterface localJusticeAreaService) {
        this.localJusticeAreaService = localJusticeAreaService;
    }

    @GetMapping(value = "/{localJusticeAreaId}")
    @Operation(summary = "Returns the LocalJusticeArea for the given localJusticeAreaId.")
    public ResponseEntity<LocalJusticeAreaEntity> getLocalJusticeAreaById(@PathVariable Short localJusticeAreaId) {

        log.info(":GET:getLocalJusticeAreaById: localJusticeAreaId: {}", localJusticeAreaId);

        LocalJusticeAreaEntity response = localJusticeAreaService.getLocalJusticeArea(localJusticeAreaId);

        return buildResponse(response);
    }

    @PostMapping(value = "/search", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Searches LocalJusticeAreas based upon criteria in request body")
    public ResponseEntity<List<LocalJusticeAreaEntity>> postLocalJusticeAreasSearch(
        @RequestBody LocalJusticeAreaSearchDto criteria) {
        log.info(":POST:postLocalJusticeAreasSearch: query: \n{}", criteria);

        List<LocalJusticeAreaEntity> response = localJusticeAreaService.searchLocalJusticeAreas(criteria);

        return buildResponse(response);
    }


}
