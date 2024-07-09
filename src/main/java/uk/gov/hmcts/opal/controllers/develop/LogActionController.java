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
import uk.gov.hmcts.opal.dto.search.LogActionSearchDto;
import uk.gov.hmcts.opal.entity.LogActionEntity;
import uk.gov.hmcts.opal.service.LogActionServiceInterface;

import java.util.List;

import static uk.gov.hmcts.opal.util.HttpUtil.buildResponse;


@RestController
@RequestMapping("/api/log-action")
@Slf4j(topic = "LogActionController")
@Tag(name = "LogAction Controller")
public class LogActionController {

    private final LogActionServiceInterface logActionService;

    public LogActionController(@Qualifier("logActionServiceProxy") LogActionServiceInterface logActionService) {
        this.logActionService = logActionService;
    }

    @GetMapping(value = "/{logActionId}")
    @Operation(summary = "Returns the LogAction for the given logActionId.")
    public ResponseEntity<LogActionEntity> getLogActionById(@PathVariable Short logActionId) {

        log.info(":GET:getLogActionById: logActionId: {}", logActionId);

        LogActionEntity response = logActionService.getLogAction(logActionId);

        return buildResponse(response);
    }

    @PostMapping(value = "/search", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Searches LogActions based upon criteria in request body")
    public ResponseEntity<List<LogActionEntity>> postLogActionsSearch(@RequestBody LogActionSearchDto criteria) {
        log.info(":POST:postLogActionsSearch: query: \n{}", criteria);

        List<LogActionEntity> response = logActionService.searchLogActions(criteria);

        return buildResponse(response);
    }


}
