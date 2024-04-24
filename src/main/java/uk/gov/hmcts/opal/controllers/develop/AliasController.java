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
import uk.gov.hmcts.opal.dto.search.AliasSearchDto;
import uk.gov.hmcts.opal.entity.AliasEntity;
import uk.gov.hmcts.opal.service.AliasServiceInterface;

import java.util.List;

import static uk.gov.hmcts.opal.util.HttpUtil.buildResponse;


@RestController
@RequestMapping("/api/alias")
@Slf4j(topic = "AliasController")
@Tag(name = "Alias Controller")
public class AliasController {

    private final AliasServiceInterface aliasService;

    public AliasController(@Qualifier("aliasService") AliasServiceInterface aliasService) {
        this.aliasService = aliasService;
    }

    @GetMapping(value = "/{aliasId}")
    @Operation(summary = "Returns the Alias for the given aliasId.")
    public ResponseEntity<AliasEntity> getAliasById(@PathVariable Long aliasId) {

        log.info(":GET:getAliasById: aliasId: {}", aliasId);

        AliasEntity response = aliasService.getAlias(aliasId);

        return buildResponse(response);
    }

    @PostMapping(value = "/search", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Searches Aliased based upon criteria in request body")
    public ResponseEntity<List<AliasEntity>> postAliasesSearch(@RequestBody AliasSearchDto criteria) {
        log.info(":POST:postAliasesSearch: query: \n{}", criteria);

        List<AliasEntity> response = aliasService.searchAliass(criteria);

        return buildResponse(response);
    }


}
