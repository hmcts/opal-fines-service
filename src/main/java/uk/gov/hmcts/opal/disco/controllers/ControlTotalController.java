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
import uk.gov.hmcts.opal.dto.search.ControlTotalSearchDto;
import uk.gov.hmcts.opal.entity.ControlTotalEntity;
import uk.gov.hmcts.opal.disco.ControlTotalServiceInterface;

import java.util.List;

import static uk.gov.hmcts.opal.util.HttpUtil.buildResponse;


@RestController
@RequestMapping("/dev/control-totals")
@Slf4j(topic = "ControlTotalController")
@Tag(name = "ControlTotal Controller")
public class ControlTotalController {

    private final ControlTotalServiceInterface controlTotalService;

    public ControlTotalController(@Qualifier("controlTotalService") ControlTotalServiceInterface controlTotalService) {
        this.controlTotalService = controlTotalService;
    }

    @GetMapping(value = "/{controlTotalId}")
    @Operation(summary = "Returns the ControlTotal for the given controlTotalId.")
    public ResponseEntity<ControlTotalEntity> getControlTotalById(@PathVariable Long controlTotalId) {

        log.debug(":GET:getControlTotalById: controlTotalId: {}", controlTotalId);

        ControlTotalEntity response = controlTotalService.getControlTotal(controlTotalId);

        return buildResponse(response);
    }

    @PostMapping(value = "/search", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Searches Control Totals based upon criteria in request body")
    public ResponseEntity<List<ControlTotalEntity>> postControlTotalsSearch(@RequestBody
                                                                                ControlTotalSearchDto criteria) {
        log.debug(":POST:postControlTotalsSearch: query: \n{}", criteria);

        List<ControlTotalEntity> response = controlTotalService.searchControlTotals(criteria);

        return buildResponse(response);
    }


}
