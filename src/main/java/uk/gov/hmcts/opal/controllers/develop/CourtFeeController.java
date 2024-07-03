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
import uk.gov.hmcts.opal.dto.search.CourtFeeSearchDto;
import uk.gov.hmcts.opal.entity.CourtFeeEntity;
import uk.gov.hmcts.opal.service.CourtFeeServiceInterface;

import java.util.List;

import static uk.gov.hmcts.opal.util.HttpUtil.buildResponse;


@RestController
@RequestMapping("/dev/court-fee")
@Slf4j(topic = "CourtFeeController")
@Tag(name = "Court Fee Controller")
public class CourtFeeController {

    private final CourtFeeServiceInterface courtFeeService;

    public CourtFeeController(@Qualifier("courtFeeServiceProxy") CourtFeeServiceInterface courtFeeService) {
        this.courtFeeService = courtFeeService;
    }

    @GetMapping(value = "/{courtFeeId}")
    @Operation(summary = "Returns the CourtFee for the given courtFeeId.")
    public ResponseEntity<CourtFeeEntity> getCourtFeeById(@PathVariable Long courtFeeId) {

        log.info(":GET:getCourtFeeById: courtFeeId: {}", courtFeeId);

        CourtFeeEntity response = courtFeeService.getCourtFee(courtFeeId);

        return buildResponse(response);
    }

    @PostMapping(value = "/search", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Searches CourtFees based upon criteria in request body")
    public ResponseEntity<List<CourtFeeEntity>> postCourtFeesSearch(@RequestBody CourtFeeSearchDto criteria) {
        log.info(":POST:postCourtFeesSearch: query: \n{}", criteria);

        List<CourtFeeEntity> response = courtFeeService.searchCourtFees(criteria);

        return buildResponse(response);
    }


}
