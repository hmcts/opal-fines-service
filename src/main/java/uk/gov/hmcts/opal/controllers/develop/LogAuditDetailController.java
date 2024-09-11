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
import uk.gov.hmcts.opal.dto.search.LogAuditDetailSearchDto;
import uk.gov.hmcts.opal.entity.LogAuditDetailEntity;
import uk.gov.hmcts.opal.service.LogAuditDetailServiceInterface;

import java.util.List;

import static uk.gov.hmcts.opal.util.HttpUtil.buildResponse;


@RestController
@RequestMapping("/dev/log-audit-details")
@Slf4j(topic = "LogAuditDetailController")
@Tag(name = "LogAuditDetail Controller")
public class LogAuditDetailController {

    private final LogAuditDetailServiceInterface logAuditDetailService;

    public LogAuditDetailController(
        @Qualifier("logAuditDetailServiceProxy") LogAuditDetailServiceInterface logAuditDetailService) {
        this.logAuditDetailService = logAuditDetailService;
    }

    @GetMapping(value = "/{logAuditDetailId}")
    @Operation(summary = "Returns the LogAuditDetail for the given logAuditDetailId.")
    public ResponseEntity<LogAuditDetailEntity> getLogAuditDetailById(@PathVariable Long logAuditDetailId) {

        log.info(":GET:getLogAuditDetailById: logAuditDetailId: {}", logAuditDetailId);

        LogAuditDetailEntity response = logAuditDetailService.getLogAuditDetail(logAuditDetailId);

        return buildResponse(response);
    }

    @PostMapping(value = "/search", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Searches LogAuditDetails based upon criteria in request body")
    public ResponseEntity<List<LogAuditDetailEntity>> postLogAuditDetailsSearch(
        @RequestBody LogAuditDetailSearchDto criteria) {
        log.info(":POST:postLogAuditDetailsSearch: query: \n{}", criteria);

        List<LogAuditDetailEntity> response = logAuditDetailService.searchLogAuditDetails(criteria);

        return buildResponse(response);
    }


}
