package uk.gov.hmcts.opal.disco.controllers;

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
import uk.gov.hmcts.opal.dto.search.HmrcRequestSearchDto;
import uk.gov.hmcts.opal.entity.HmrcRequestEntity;
import uk.gov.hmcts.opal.disco.HmrcRequestServiceInterface;

import java.util.List;

import static uk.gov.hmcts.opal.util.HttpUtil.buildResponse;


@RestController
@RequestMapping("/dev/hmrc-requests")
@Slf4j(topic = "HmrcRequestController")
@Tag(name = "HmrcRequest Controller")
public class HmrcRequestController {

    private final HmrcRequestServiceInterface hmrcRequestService;

    public HmrcRequestController(@Qualifier("hmrcRequestService") HmrcRequestServiceInterface hmrcRequestService) {
        this.hmrcRequestService = hmrcRequestService;
    }

    @GetMapping(value = "/{hmrcRequestId}")
    @Operation(summary = "Returns the HmrcRequest for the given hmrcRequestId.")
    public ResponseEntity<HmrcRequestEntity> getHmrcRequestById(@PathVariable Long hmrcRequestId) {

        log.debug(":GET:getHmrcRequestById: hmrcRequestId: {}", hmrcRequestId);

        HmrcRequestEntity response = hmrcRequestService.getHmrcRequest(hmrcRequestId);

        return buildResponse(response);
    }

    @PostMapping(value = "/search", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Searches HMRC Requests based upon criteria in request body")
    public ResponseEntity<List<HmrcRequestEntity>> postHmrcRequestsSearch(@RequestBody HmrcRequestSearchDto criteria) {
        log.debug(":POST:postHmrcRequestsSearch: query: \n{}", criteria);

        List<HmrcRequestEntity> response = hmrcRequestService.searchHmrcRequests(criteria);

        return buildResponse(response);
    }


}
