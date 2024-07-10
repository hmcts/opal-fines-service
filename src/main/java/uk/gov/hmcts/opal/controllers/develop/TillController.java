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
import uk.gov.hmcts.opal.dto.search.TillSearchDto;
import uk.gov.hmcts.opal.entity.TillEntity;
import uk.gov.hmcts.opal.service.TillServiceInterface;

import java.util.List;

import static uk.gov.hmcts.opal.util.HttpUtil.buildResponse;


@RestController
@RequestMapping("/dev/till")
@Slf4j(topic = "TillController")
@Tag(name = "Till Controller")
public class TillController {

    private final TillServiceInterface tillService;

    public TillController(@Qualifier("tillServiceProxy") TillServiceInterface tillService) {
        this.tillService = tillService;
    }

    @GetMapping(value = "/{tillId}")
    @Operation(summary = "Returns the Till for the given tillId.")
    public ResponseEntity<TillEntity> getTillById(@PathVariable Long tillId) {

        log.info(":GET:getTillById: tillId: {}", tillId);

        TillEntity response = tillService.getTill(tillId);

        return buildResponse(response);
    }

    @PostMapping(value = "/search", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Searches Tills based upon criteria in request body")
    public ResponseEntity<List<TillEntity>> postTillsSearch(@RequestBody TillSearchDto criteria) {
        log.info(":POST:postTillsSearch: query: \n{}", criteria);

        List<TillEntity> response = tillService.searchTills(criteria);

        return buildResponse(response);
    }


}
