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
import uk.gov.hmcts.opal.dto.search.EnforcementSearchDto;
import uk.gov.hmcts.opal.entity.EnforcementEntity;
import uk.gov.hmcts.opal.service.EnforcementServiceInterface;

import java.util.List;

import static uk.gov.hmcts.opal.util.HttpUtil.buildResponse;


@RestController
@RequestMapping("/dev/enforcements")
@Slf4j(topic = "EnforcementController")
@Tag(name = "Enforcement Controller")
public class EnforcementController {

    private final EnforcementServiceInterface enforcementService;

    public EnforcementController(@Qualifier("enforcementServiceProxy") EnforcementServiceInterface enforcementService) {
        this.enforcementService = enforcementService;
    }

    @GetMapping(value = "/{enforcementId}")
    @Operation(summary = "Returns the Enforcement for the given enforcementId.")
    public ResponseEntity<EnforcementEntity> getEnforcementById(@PathVariable Long enforcementId) {

        log.info(":GET:getEnforcementById: enforcementId: {}", enforcementId);

        EnforcementEntity response = enforcementService.getEnforcement(enforcementId);

        return buildResponse(response);
    }

    @PostMapping(value = "/search", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Searches Enforcements based upon criteria in request body")
    public ResponseEntity<List<EnforcementEntity>> postEnforcementsSearch(@RequestBody EnforcementSearchDto criteria) {
        log.info(":POST:postEnforcementsSearch: query: \n{}", criteria);

        List<EnforcementEntity> response = enforcementService.searchEnforcements(criteria);

        return buildResponse(response);
    }


}
