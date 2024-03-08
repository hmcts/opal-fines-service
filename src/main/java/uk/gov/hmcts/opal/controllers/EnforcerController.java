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
import uk.gov.hmcts.opal.dto.search.EnforcerSearchDto;
import uk.gov.hmcts.opal.entity.EnforcerEntity;
import uk.gov.hmcts.opal.service.EnforcerServiceInterface;

import java.util.List;

import static uk.gov.hmcts.opal.util.HttpUtil.buildResponse;


@RestController
@RequestMapping("/api/enforcer")
@Slf4j(topic = "EnforcerController")
@Tag(name = "Enforcer Controller")
public class EnforcerController {

    private final EnforcerServiceInterface enforcerService;

    public EnforcerController(@Qualifier("enforcerServiceProxy") EnforcerServiceInterface enforcerService) {
        this.enforcerService = enforcerService;
    }

    @GetMapping(value = "/{enforcerId}")
    @Operation(summary = "Returns the Enforcer for the given enforcerId.")
    public ResponseEntity<EnforcerEntity> getEnforcerById(@PathVariable Long enforcerId) {

        log.info(":GET:getEnforcerById: enforcerId: {}", enforcerId);

        EnforcerEntity response = enforcerService.getEnforcer(enforcerId);

        return buildResponse(response);
    }

    @PostMapping(value = "/search", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Searches Enforcers based upon criteria in request body")
    public ResponseEntity<List<EnforcerEntity>> postEnforcersSearch(@RequestBody EnforcerSearchDto criteria) {
        log.info(":POST:postEnforcersSearch: query: \n{}", criteria);

        List<EnforcerEntity> response = enforcerService.searchEnforcers(criteria);

        return buildResponse(response);
    }


}
